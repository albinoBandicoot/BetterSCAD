/* An alternative to the gear-based bearings that instead uses rollers. 
I don't know how well it would work, but it looks like it could be made 
substantially smaller than the gear-based bearings. The values given here
seem pretty reasonable and produce a bearing that's only 0.78 inches in
diameter.
*/

module stack (h, mr, or) {
	translate([0,0,-h/2])cylinder (r=or, r2=mr, h=h/2+dh, $fn=32);
	translate([0,0,0]) cylinder(r=mr, r2=or, h=h/2, $fn=32);
}

cmr = 3.5;	// center middle radius
dr = 1;	// change in radius from middle to top
ht = 8;	// height of bearing
rr = 2;	// roller middle radius
or = 10;	// outside radius
br = 2;	// bore radius
n=8;		// number of rollers
gap = 0.1;	// space between rollers and the other two components
pretty=true;
dh = pretty ? 0.01 : 0;
module bearing (){

// central part
difference () {
	stack (ht, cmr, cmr+dr);
	cylinder(h=50, r=br, center=true, $fn=6);
}

// bearing rollers
for (i=[0:n]){
	rotate([0,0,i*360/n+10]){
		translate([cmr + rr + gap, 0, 0]) stack (ht, rr, rr - dr);
	}
}

// outer part
difference () {
	cylinder (r = or, h = ht, center=true, $fn=32);
	stack (h=ht+dh, mr = cmr + 2*rr + 2 * gap, or = cmr + 2*(rr - dr) + dr + 2*gap);
}

}
//projection (cut=true){
scale(20) bearing();

