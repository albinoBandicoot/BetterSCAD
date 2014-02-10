module fipple () {
	difference () {
		rotate([0,90,0]) cylinder(r=11, h=40, $fn=80);	// base
		translate([0,0,-8]) rotate([90,0,0]) cylinder(r=15, h=50, center=true, $fn=80);	// main lip c	utout	
		translate([0,0,8.75]) cube([42, 6, 2], center=true);	// air path
		translate([0,21,0]) cylinder(r=15, h=50, center=true, $fn=80);	// side cutouts
		translate([0,-21,0]) cylinder(r=15, h=50, center=true, $fn=80);
	
		translate([17.5,0,0]) difference () {	// interior bore
			rotate([0,90,0]) cylinder(r=9, h=50, $fn=80);	// main cylinder
			translate([-0.04,0,-1]) rotate([90,0,0]) interior_cutout(rad=8,h=50);	// curve at front
			*difference () {	// flat part at front to position the blade in the airstream
				translate([0,0,8]) cube([150,20,5], center=true);
				translate([0,0,-15]) rotate([0,-10,0]) cube([200,25,20], center=true);	// angled par	t to transition smoothly back into cylindrical	
			}	
		}
	
		translate([22, 0, 12.4]) rotate([0,-10,0]) cube([12, 6, 5], center=true);	// blade
	}
}

module interior_cutout (rad, h) {
		union () {
			intersection () {
				translate([0,0,-h/2]) cube([rad,rad,h]);
				cylinder(r=rad, h=h+1, center=true, $fn=100);
			}
			difference () {
				translate([0,-rad,-h/2]) cube([2*rad,rad,h]);
				translate([2*rad,0,0]) cylinder (r=rad, h=h+1, center=true, $fn=100);
			}
		}
}

fipple ();
