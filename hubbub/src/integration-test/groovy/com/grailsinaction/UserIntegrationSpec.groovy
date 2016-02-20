package com.grailsinaction


import grails.test.mixin.integration.Integration
import grails.transaction.*
import spock.lang.*

@Integration
@Rollback
class UserIntegrationSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    def 'saving a user to the database'()
    {
        given: 'i have a new user'
        def joe = new User(loginId: 'joe', password: 'secret')
        when: 'the user is saved'
        joe.save()
        then: 'no errors saving user'
        joe.errors.errorCount == 0
        and: 'user is in database'
        joe.id != null
        and: 'the user can be retrieved from the database'
        User.get(joe.id).loginId == joe.loginId
    }
    def 'updating a saved user updates the users properties'()
    {
        given: 'an existing user in the database'
        def joe = new User(loginId: 'joe', password: 'secret').save()
        when: 'joes password is changed'
        def existingUser = User.get(joe.id)
        existingUser.password = 'password1'
        and: 'joes new password is updated to the database'
        existingUser.save(failOnError:true)
        then: 'new password can be queried'
        User.get(existingUser.id).password == 'password1'
    }
    def 'deleting an existing user from the database'()
    {
        given: 'an existing user in the database'
        def joe = new User(loginId: 'joe', password: 'secret')
        joe.save(failOnError: true)
        when: 'the user is deleted'
        def user = User.get(joe.id)
        user.delete(flush: true)
        then: 'the user is removed from the database'
        !User.exists(user.id)
    }
    def 'validating a user that fails password and homepage validation requirements'()
    {
        given: 'a user that fails validation requirements'
        def badUser = new User(loginId: 'joe', password: 'tiny')
        badUser.save()
        when: 'validate user'
        badUser.validate()
        then: 'user has errors'
        badUser.hasErrors()

        then: 'the following errors exist'
        'size.toosmall' == badUser.errors.getFieldError('password').code
        'tiny' == badUser.errors.getFieldError('password').rejectedValue

        and: 'loginId has no errors associated with it and was accepted'
        !badUser.errors.getFieldError('loginId')

    }
    def "Recovering from a failed save by fixing invalid properties"() {

        given: "A user that has invalid properties"
        def chuck = new User(loginId: 'chuck', password: 'tiny')
        assert chuck.save()  == null
        assert chuck.hasErrors()

        when: "We fix the invalid properties"
        chuck.password = "fistfist"
        chuck.validate()

        then: "The user saves and validates fine"
        !chuck.hasErrors()
        chuck.save()

    }

    def "Ensure a user can follow other users"() {

        given: "A set of baseline users"
        def joe = new User(loginId: 'joe', password:'password').save()
        def jane = new User(loginId: 'jane', password:'password').save()
        def jill = new User(loginId: 'jill', password:'password').save()

        when: "Joe follows Jane & Jill, and Jill follows Jane"
        joe.addToFollowing(jane)
        joe.addToFollowing(jill)
        jill.addToFollowing(jane)

        then: "Follower counts should match following people"
        2 == joe.following.size()
        1 == jill.following.size()

    }

}
