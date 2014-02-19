function gcd (x,y) = x == 0 ? y : gcd (y%x, x);
function f (x) = x > 0 ? x > 10 ? 5 : 3 : 2;

sphere (r = f(30));
sphere (r = f(4));
sphere (r = f(-3));

