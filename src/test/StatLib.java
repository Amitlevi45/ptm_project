package test;

class StatLib {
	// simple average
	public static float avg(float[] x)
	{
		float start = 0;
		for (int i = 0; i < x.length; i++)
		{
			start += x[i];
		}
		
		return (start / x.length);
	}
	
	// returns the variance of X and Y
	public static float var(float[] x)
	{
		float start = 0;
		for (int i = 0; i < x.length; i++)
		{
			start += (x[i] * x[i]);
		}
		
		return ((start / x.length) - ((float) Math.pow(avg(x), 2)));
	}
	
	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y)
	{
		float start = 0;

		for (int i = 0; i < x.length; i++)
		{
			start += x[i] * y[i];
		}
		start /= x.length;
		
		return (start - avg(x) * avg(y));
	}
	
	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y)
	{
		return (cov(x, y) / (float) (Math.sqrt(var(x)) * Math.sqrt(var(y))));
	}
	
	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points)
	{
		float[] x = new float[points.length];
		float[] y = new float[points.length]; // TOOD: make this work without crashing
		
		for (int i = 0; i < points.length; i++)
		{
			x[i] = points[i].x;
			y[i] = points[i].y;
		}
		
		float a = cov(x,y) / var(x);
		float b = avg(y) - (a * avg(x));
		Line l = new Line(a,b);
		
		return l;
	}
	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p,Point[] points)
	{
		Line l = linear_reg(points);
		return dev(p, l);
	}
	// returns the deviation between point p and the line
	public static float dev(Point p,Line l)
	{
		return Math.abs((l.f(p.x) - p.y));
	}
}
