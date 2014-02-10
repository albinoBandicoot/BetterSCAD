nteeth = 28;
tooth_ht = 3;
tooth_basew = 4;
tooth_topw = 2;
inner_r = 25;
flange_r = 42;
hub_r = 18;
bore_r = 10;
beltway_wd = 12;
flange_ht = 5;
hub_ht = 15;
tot_ht = beltway_wd + 2*flange_ht + hub_ht;
key_wd = 5;
module tooth (ht, base_wd, top_wd, inner_rad){
	d = sqrt(inner_rad*inner_rad - (base_wd/2)*(base_wd/2));
	polygon(points=[[-base_wd/2, 0],[-base_wd/2, d],[-top_wd/2, d+ht],[top_wd/2, d+ht],[base_wd/2, d],[base_wd/2,0]]);
}

module teeth (){
	for (i=[0:nteeth]){
		rotate([0,0,i*360.0/nteeth]){
			linear_extrude(height=beltway_wd, convexity=5){
				tooth (tooth_ht, tooth_basew, tooth_topw, inner_r);
			}
		}
	}
}

difference (){
	union (){
		cylinder(r=flange_r, h=flange_ht);
//		translate([0,0,flange_ht + beltway_wd]){
//			cylinder(r=flange_r, h=flange_ht);
//		}
		translate([0,0,2*flange_ht + beltway_wd]){
			cylinder(r=hub_r, h=hub_ht);
		}
		translate([0,0,flange_ht]){
			cylinder(r=inner_r, h=beltway_wd);
			teeth();
		}
	}
	cylinder(r=bore_r, h=100, center=true);
	translate([bore_r-key_wd/2,-key_wd/2,-0.05]){
		cube([key_wd, key_wd, 100]);
	}
}