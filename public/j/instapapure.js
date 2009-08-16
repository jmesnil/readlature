jQuery(document).ready(function() {

$("a.star, a.delete").mouseover(function() {
  $(this).addClass("cursored");
});

toggleStar = function(star) {
  starred = !star.hasClass("starred");
  char = (starred ? "&#9733;" : "&#9734;");
  star.html(char);
  return starred;
}

$("a.star").click(function() {
  star = $(this);
  starred = toggleStar(star);
  id = star.parents(".article").attr("id");
  $.post("/api/article/" + id, {id: id, st: starred},
    function(data) {
      star.toggleClass("starred");
    });
});

$("a.delete").click(function() {
  title = $(this).prev("a").text();
  if (confirm('Are you sure you want to delete "' + title + '"?'))
  {
    article = $(this).parents(".article");
    id = article.attr("id");
    $.post("/api/article/delete", {id: id},
      function(data) {
        article.slideToggle("slow");
      });
  }
});

});