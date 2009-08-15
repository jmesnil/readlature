== Server ==

start the server:
./server

== Client ==

* show all articles

open http://localhost:8080/

* show starred articles

open http://localhost:8080/s

* create a new article:

open http://localhost:8080/article
curl -d l=http://example.com -d t="Title of the page" -d s="summary" http://localhost:8080/article

