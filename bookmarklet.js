// javascript:var%20d=document,uri%20=%20%27http://localhost:8080/post%27;f%20=%20d.createElement(%27form%27);f.method%20=%20%27post%27;f.action%20=%20uri;t%20=d.createElement(%27input%27);t.type%20=%20%27hidden%27;t.name%20=%20%27t%27;t.value%20=d.title;l%20=%20d.createElement(%27input%27);l.type%20=%20%27hidden%27;l.name%20=%20%27l%27;l.value%20=%20d.location.href;s%20=%20d.createElement(%27input%27);s.type%20=%20%27hidden%27;s.name%20=%20%27s%27;s.value%20=%20getSelection%20();f.appendChild(t);f.appendChild(l);f.appendChild(s);b%20=%20d.createElement(%27body%27);b.appendChild(f);h%20=d.getElementsByTagName(%27html%27)[0];h.appendChild(b);f.submit();void(0)
var d=document,uri = 'http://localhost:8080/post';
f = d.createElement('form');
f.method = 'post';
f.action = uri;
t =d.createElement('input');
t.type = 'hidden';
t.name = 't';
t.value = d.title;
l = d.createElement('input');
l.type = 'hidden';
l.name = 'l';
l.value = d.location.href;
s = d.createElement('input');
s.type = 'hidden';
s.name = 's';
s.value = getSelection ();
f.appendChild(t);
f.appendChild(l);
f.appendChild(s);
b = d.createElement('body');
b.appendChild(f);
h =d.getElementsByTagName('html')[0];
h.appendChild(b);
f.submit();
void(0)