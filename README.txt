== Server ==

start the server:
./server

== Client ==

* show unread articles

open http://localhost:8080/unread

* show starred articles

open http://localhost:8080/starred

* show archive

open http://localhost:8080/archive

* create a new article:

open http://localhost:8080/article

curl -d l=http://example.com -d t="Title of the page" -d s="summary" http://localhost:8080/api/article

or use the bookmarklet: it is simpler. 
If you select text on a page before using the bookmarklet, the text will
be used as the summary of the page.
