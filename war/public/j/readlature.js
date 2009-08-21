jQuery(document).ready(function() {

toggleHelp = function() {
  $(".help").toggle($(".article").length == 0);
}

$("img.star, img.star-empty, a.delete").mouseover(function() {
  $(this).addClass("cursored");
});

$("a.bookmarklet").click(function(e) {
  e.preventDefault();
  alert("Drag this link to your Bookmarks bar to install it.");
});

star = function(article, starred)
{
  id = article.attr("id");
  $.post("/api/article/" + id, {id: id, starred: starred},
    function(data) {
      if (starred) {
        article.find('img.star-empty').hide().end().find('img.star').show();
      } else {
        article.find('img.star-empty').show().end().find('img.star').hide();
        if (document.location.href.match(/\/starred$/))
        {
          article.slideToggle(function() {
            article.remove();
            toggleHelp();
          });
        }
      }
    });
}

$("img.star-empty").click(function() {
  article = $(this).parents(".article");
  star(article, true);
});

$("img.star").click(function() {
  article = $(this).parents(".article");
  star(article, false);
});

/* update the status to 'read' before going to the article */
$("a.unread").click(function(e) {
  e.preventDefault();
  href = $(this).attr("href");
  id = $(this).parents(".article").attr("id");
  $.post("/api/article/" + id, {id: id, unread: false},
    function(data) {
      top.location.href = href;
    });
});

$("img.delete").click(function() {
  title = $(this).prev("a").text();
  if (confirm('Are you sure you want to delete "' + title + '"?'))
  {
    article = $(this).parents(".article");
    id = article.attr("id");
    $.post("/api/article/delete", {id: id},
      function(data) {
        article.slideToggle(function() {
          article.remove();
          toggleHelp();
        });
      });
  }
});

toggleHelp();

});