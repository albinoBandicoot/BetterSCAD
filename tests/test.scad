intersection () {
	i = 12;
	sphere (r=5);
	sphere (r=2*i);
	f (5);
}

module f () {
	sphere (r=i);
}
