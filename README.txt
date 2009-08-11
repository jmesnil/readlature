== Server ==

start the server:
./server

== Client ==

* show all posts

open http://localhost:8080/posts

post a new page:

open http://localhost:8080/post
curl -d l=http://example.com -d t="Title of the page" http://localhost:8080/post

