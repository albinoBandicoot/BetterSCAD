module a () {
	x = 10;
	if (x == 5) {
		module b () {
			sphere (1);
		}
		b();
	}
}
		
