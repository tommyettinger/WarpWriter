//port of https://github.com/mcychan/nQuant.j2se
//Apache 2.0 license
package org.cie;

import com.badlogic.gdx.graphics.Color;

public class CIELABConverter {

	protected double a1Prime, a2Prime, CPrime1, CPrime2, barCPrime, barhPrime;
	public CIELABConverter()
	{
	}

	public static class Lab {
		public double alpha;
		public double A;
		public double B;
		public double L;
		public Lab()
		{
			alpha = 1.0;
			A = 0.0;
			B = 0.0;
			L = 0.0;
		}
		public Lab(double L, double A, double B, double alpha)
		{
			this.L = L;
			this.A = A;
			this.B = B;
			this.alpha = alpha;
		}
		public Lab(Color color)
		{
			double r = color.r, g = color.g, b = color.b;
			alpha = color.a;
			double x, y, z;

			r = (r > 0.04045) ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92;
			g = (g > 0.04045) ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92;
			b = (b > 0.04045) ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92;

			x = (r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047;
			y = (r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.00000;
			z = (r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883;

			x = (x > 0.008856) ? Math.cbrt(x) : (7.787 * x) + 16.0 / 116.0;
			y = (y > 0.008856) ? Math.cbrt(y) : (7.787 * y) + 16.0 / 116.0;
			z = (z > 0.008856) ? Math.cbrt(z) : (7.787 * z) + 16.0 / 116.0;
			
			L = (116.0 * y) - 16.0;
			A = 500.0 * (x - y);
			B = 200.0 * (y - z);
		}
		public Color toColor(){
			double y = (L + 16.0) / 116.0;
			double x = A / 500.0 + y;
			double z = y - B / 200.0;
			double r, g, b;

			x = 0.95047 * ((x * x * x > 0.008856) ? x * x * x : (x - 16.0 / 116.0) / 7.787);
			y = 1.00000 * ((y * y * y > 0.008856) ? y * y * y : (y - 16.0 / 116.0) / 7.787);
			z = 1.08883 * ((z * z * z > 0.008856) ? z * z * z : (z - 16.0 / 116.0) / 7.787);

			r = x *  3.2406 + y * -1.5372 + z * -0.4986;
			g = x * -0.9689 + y *  1.8758 + z *  0.0415;
			b = x *  0.0557 + y * -0.2040 + z *  1.0570;

			r = (r > 0.0031308) ? (1.055 * Math.pow(r, 1.0 / 2.4) - 0.055) : 12.92 * r;
			g = (g > 0.0031308) ? (1.055 * Math.pow(g, 1.0 / 2.4) - 0.055) : 12.92 * g;
			b = (b > 0.0031308) ? (1.055 * Math.pow(b, 1.0 / 2.4) - 0.055) : 12.92 * b;

			return new Color((float)r, (float)g, (float)b, (float)alpha);
		}

	}
	
	public static Lab RGB2LAB(final double[] c)
	{
		double r = c[0], g = c[1], b = c[3];
		double x, y, z;

		r = (r > 0.04045) ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92;
		g = (g > 0.04045) ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92;
		b = (b > 0.04045) ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92;

		x = (r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047;
		y = (r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.00000;
		z = (r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883;

		x = (x > 0.008856) ? Math.cbrt(x) : (7.787 * x) + 16.0 / 116.0;
		y = (y > 0.008856) ? Math.cbrt(y) : (7.787 * y) + 16.0 / 116.0;
		z = (z > 0.008856) ? Math.cbrt(z) : (7.787 * z) + 16.0 / 116.0;

		Lab lab = new Lab();
		lab.alpha = c[3];
		lab.L = (116 * y) - 16;
		lab.A = 500 * (x - y);
		lab.B = 200 * (y - z);
		return lab;
	}

	public static double[] LAB2RGB(final Lab lab){
		double y = (lab.L + 16.0) / 116.0;
		double x = lab.A / 500.0 + y;
		double z = y - lab.B / 200.0;
		double r, g, b;

		x = 0.95047 * ((x * x * x > 0.008856) ? x * x * x : (x - 16.0 / 116.0) / 7.787);
		y = 1.00000 * ((y * y * y > 0.008856) ? y * y * y : (y - 16.0 / 116.0) / 7.787);
		z = 1.08883 * ((z * z * z > 0.008856) ? z * z * z : (z - 16.0 / 116.0) / 7.787);

		r = x *  3.2406 + y * -1.5372 + z * -0.4986;
		g = x * -0.9689 + y *  1.8758 + z *  0.0415;
		b = x *  0.0557 + y * -0.2040 + z *  1.0570;

		r = (r > 0.0031308) ? (1.055 * Math.pow(r, 1.0 / 2.4) - 0.055) : 12.92 * r;
		g = (g > 0.0031308) ? (1.055 * Math.pow(g, 1.0 / 2.4) - 0.055) : 12.92 * g;
		b = (b > 0.0031308) ? (1.055 * Math.pow(b, 1.0 / 2.4) - 0.055) : 12.92 * b;

		return new double[]{Math.max(0.0, Math.min(1.0, r)), Math.max(0.0, Math.min(1.0, g)), Math.max(0.0, Math.min(1.0, b)), Math.max(0.0, Math.min(1.0, lab.alpha))};
	}
	
	/*******************************************************************************
	* Conversions.
	******************************************************************************/

	private static double deg2Rad(final double deg)
	{
		return (deg * (Math.PI / 180.0));
	}

	static double L_prime_div_k_L_S_L(final Lab lab1, final Lab lab2)
	{
		final double k_L = 1.0;
		double deltaLPrime = lab2.L - lab1.L;	
		double barLPrime = (lab1.L + lab2.L) / 2.0;
		double S_L = 1 + ((0.015 * Math.pow(barLPrime - 50.0, 2.0)) / Math.sqrt(20 + Math.pow(barLPrime - 50.0, 2.0)));
		return deltaLPrime / (k_L * S_L);
	}

	protected double C_prime_div_k_L_S_L(final Lab lab1, final Lab lab2)
	{
		final double k_C = 1.0;
		final double pow25To7 = 6103515625.0; /* pow(25, 7) */
		double C1 = Math.sqrt((lab1.A * lab1.A) + (lab1.B * lab1.B));
		double C2 = Math.sqrt((lab2.A * lab2.A) + (lab2.B * lab2.B));
		double barC = (C1 + C2) * 0.5;
		double G = 0.5 * (1 - Math.sqrt(Math.pow(barC, 7) / (Math.pow(barC, 7) + pow25To7)));
		a1Prime = ((1.0 + G) * lab1.A);
		a2Prime = ((1.0 + G) * lab2.A);

		CPrime1 = (Math.sqrt((a1Prime * a1Prime) + (lab1.B * lab1.B)));
		CPrime2 = (Math.sqrt((a2Prime * a2Prime) + (lab2.B * lab2.B)));
		double deltaCPrime = CPrime2 - CPrime1;
		double barCPrime =  (CPrime1 + CPrime2) * 0.5;
		
		double S_C = 1 + (0.045 * barCPrime);
		return deltaCPrime / (k_C * S_C);
	}

	protected double H_prime_div_k_L_S_L(final Lab lab1, final Lab lab2)
	{
		final double k_H = 1.0;
		final double deg360InRad = Math.PI * 2.0;
		final double deg180InRad = Math.PI;
		double CPrimeProduct = CPrime1 * CPrime2;
		double hPrime1;
		if (Math.abs(lab1.B) < 0x1p-32 && Math.abs(a1Prime) < 0x1p-32)
			hPrime1 = 0.0;
		else {
			hPrime1 = Math.atan2(lab1.B, a1Prime);
			/*
			* This must be converted to a hue angle in degrees between 0
			* and 360 by addition of 2pi to negative hue angles.
			*/
			if (hPrime1 < 0)
				hPrime1 += deg360InRad;
		}
		double hPrime2;
		if (Math.abs(lab2.B) < 0x1p-32 && Math.abs(a2Prime) < 0x1p-32)
			hPrime2 = 0.0;
		else {
			hPrime2 = Math.atan2(lab2.B, a2Prime);
			/*
			* This must be converted to a hue angle in degrees between 0
			* and 360 by addition of 2pi to negative hue angles.
			*/
			if (hPrime2 < 0)
				hPrime2 += deg360InRad;
		}
		double deltahPrime;
		if (Math.abs(CPrimeProduct) < 0x1p-32)
			deltahPrime = 0;
		else {
			deltahPrime = hPrime2 - hPrime1;
			if (deltahPrime < -deg180InRad)
				deltahPrime += deg360InRad;
			else if (deltahPrime > deg180InRad)
				deltahPrime -= deg360InRad;
		}

		double deltaHPrime = 2.0 * Math.sqrt(CPrimeProduct) * Math.sin(deltahPrime / 2.0);
		double hPrimeSum = hPrime1 + hPrime2;
		if (Math.abs(CPrime1 * CPrime2) < 0x1p-32) {
			barhPrime = hPrimeSum;
		}
		else {
			if (Math.abs(hPrime1 - hPrime2) <= deg180InRad)
				barhPrime = hPrimeSum * 0.5;
			else {
				if (hPrimeSum < deg360InRad)
					barhPrime = ((hPrimeSum + deg360InRad) * 0.5);
				else
					barhPrime = ((hPrimeSum - deg360InRad) * 0.5);
			}
		}

		barCPrime = ((CPrime1 + CPrime2) * 0.5);
		double T = 1.0 - (0.17 * Math.cos(barhPrime - deg2Rad(30.0))) +
			(0.24 * Math.cos(2.0 * barhPrime)) +
			(0.32 * Math.cos((3.0 * barhPrime) + deg2Rad(6.0))) -
			(0.20 * Math.cos((4.0 * barhPrime) - deg2Rad(63.0)));
		double S_H = 1 + (0.015 * barCPrime * T);
		return deltaHPrime / (k_H * S_H);
	}

	protected double R_T(final double C_prime_div_k_L_S_L, final double H_prime_div_k_L_S_L)
	{
		final double pow25To7 = 6103515625.0; /* Math.pow(25, 7) */
		double deltaTheta = deg2Rad(30.0) * Math.exp(-Math.pow((barhPrime - deg2Rad(275.0)) / deg2Rad(25.0), 2.0));
		double R_C = 2.0 * Math.sqrt(Math.pow(barCPrime, 7.0) / (Math.pow(barCPrime, 7.0) + pow25To7));
		double R_T = (-Math.sin(2.0 * deltaTheta)) * R_C;
		return R_T * C_prime_div_k_L_S_L * H_prime_div_k_L_S_L;
	}

	/* From the paper "The CIEDE2000 Color-Difference Formula: Implementation Notes, */
	/* Supplementary Test Data, and Mathematical Observations", by */
	/* Gaurav Sharma, Wencheng Wu and Edul N. Dalal, */
	/* Color Res. Appl., vol. 30, no. 1, pp. 21-30, Feb. 2005. */
	/* Return the CIEDE2000 Delta E color difference measure squared, for two Lab values */
	public double CIEDE2000(final Lab lab1, final Lab lab2)
	{
		double deltaL_prime_div_k_L_S_L = L_prime_div_k_L_S_L(lab1, lab2);
		double deltaC_prime_div_k_L_S_L = C_prime_div_k_L_S_L(lab1, lab2);
		double deltaH_prime_div_k_L_S_L = H_prime_div_k_L_S_L(lab1, lab2);
		double deltaR_T = R_T(deltaC_prime_div_k_L_S_L, deltaH_prime_div_k_L_S_L);
		return
			Math.pow(deltaL_prime_div_k_L_S_L, 2.0) +
			Math.pow(deltaC_prime_div_k_L_S_L, 2.0) +
			Math.pow(deltaH_prime_div_k_L_S_L, 2.0) +
			deltaR_T;
	}
}