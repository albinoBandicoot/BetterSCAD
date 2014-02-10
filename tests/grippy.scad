n = 8;
w = 2;
h = 15;
base = 3.5;
outer = 15;
wall = 0.75;
translate([0,0,h/2]) difference () {
	cylinder(r=outer, h=h, center=true, $fn=80);
	cylinder(r=outer-2*wall, h=h+5, center=true, $fn=80);
	for (i=[0:n-1]) {
		rotate([0,0,i*360/n]) translate([outer,0,base]) cube([4*wall+0.1,w,h], center=true);
	}
}
translate([0,0,-base]) cylinder(r=outer+2, h=base, $fn=80);