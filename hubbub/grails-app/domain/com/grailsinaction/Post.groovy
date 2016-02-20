package com.grailsinaction

class Post {
    String content
    Date dateCreated


    /*
    This property is vitally important in both 1:m and m:n relationships because it tells GORM how to implement
    cascading operations. In particular, when the User is deleted, all their matching Post objects are deleted, too.
     */
    static belongsTo = [user: User] //points to owning object
    static hasMany = [tags: Tag]
    static constraints = {
        content blank: false
    }
    static mapping = {
        sort dateCreated: 'desc'
    }
}
