package com.example.botondepanico;
class Utm {

    private double[] coordenadasUMT;
    private double zona;
    private String hemisferio;

    // Eje mayor del modelo elipsoide
    final double sm_a = 6378137.0;
    // Eje menor del modelo elipsoide
    final double sm_b = 6356752.314;
    // Escala de factor UTM
    final double UTMScaleFactor = 0.9996;

    // convertir grados a radiantes
    private double DegToRad (double deg){
        return (deg / 180.0 * Math.PI);
    }

    //constructor
    Utm(double[] coordenadasGeograficas){
        coordenadasUMT = new double[2];
        this.zona = (int) Math.floor(LatLonToUTMXY(DegToRad(coordenadasGeograficas[1]), DegToRad(coordenadasGeograficas[0]), Math.floor((coordenadasGeograficas[0] + 180.0) / 6) + 1, this.coordenadasUMT));
        this.hemisferio = (coordenadasGeograficas[1] < 0)? "sur": "norte";
    }

    //devolver valor X en UMT
    double getUMTX(){
        return this.coordenadasUMT[0];
    }

    //devolver valor Y en UMT
    double getUMTY(){
        return this.coordenadasUMT[1];
    }

    //devolver hemisferio
    String getHemisferio(){
        return this.hemisferio;
    }

    //devolver la zona UMT
    double getZona(){
        return this.zona;
    }

    // Calcula la distancia elipsoidal desde el ecuador hasta un punto en una latitud dada
    // phi - Latitud del punto, en radianes
    // devuleve La distancia elipsoidal del punto desde el ecuador, en metros.
    private double ArcLengthOfMeridian (double phi) {
        double alpha, beta, gamma, delta, epsilon, n;

        n = (sm_a - sm_b) / (sm_a + sm_b);
        alpha = ((sm_a + sm_b) / 2.0) * (1.0 + (Math.pow (n, 2.0) / 4.0) + (Math.pow (n, 4.0) / 64.0));
        beta = (-3.0 * n / 2.0) + (9.0 * Math.pow (n, 3.0) / 16.0) + (-3.0 * Math.pow (n, 5.0) / 32.0);
        gamma = (15.0 * Math.pow (n, 2.0) / 16.0) + (-15.0 * Math.pow (n, 4.0) / 32.0);
        delta = (-35.0 * Math.pow (n, 3.0) / 48.0) + (105.0 * Math.pow (n, 5.0) / 256.0);
        epsilon = (315.0 * Math.pow (n, 4.0) / 512.0);
        // calcula la suma de las series
        return alpha * (phi + (beta * Math.sin (2.0 * phi)) + (gamma * Math.sin (4.0 * phi)) + (delta * Math.sin (6.0 * phi)) + (epsilon * Math.sin (8.0 * phi)));
    }

    // Determina el meridiano central para la zona UTM dada
    // zone: un valor entero que designa la zona UTM, rango [1,60].
    // El meridiano central para la zona UTM dada, en radianes o cero
    // si el parámetro de zona UTM está fuera del rango [1,60].
    // El rango del meridiano central es el equivalente en radianes de [-177, + 177]
    private double UTMCentralMeridian (double zone) {
        return DegToRad (-183.0 + (zone * 6.0));
    }


    // Convierte latitud y longitud en coordenadas x,y en proyección transversal de mercator
    // phi es la Latitud del punto, en radianes
    // lambda es la Longitud del punto, en radianes
    // lambda0: longitud del meridiano central que se utilizará, en radianes
    // Devuelve una matriz de 2 elementos que contiene las coordenadas x,y
    private void MapLatLonToXY (double phi, double lambda, double lambda0, double[] xy){
        double N, nu2, ep2, t, t2, l;
        double l3coef, l4coef, l5coef, l6coef, l7coef, l8coef;

        ep2 = (Math.pow (sm_a, 2.0) - Math.pow (sm_b, 2.0)) / Math.pow (sm_b, 2.0);
        nu2 = ep2 * Math.pow (Math.cos (phi), 2.0);
        N = Math.pow (sm_a, 2.0) / (sm_b * Math.sqrt (1 + nu2));
        t = Math.tan (phi);
        t2 = t * t;
        l = lambda - lambda0;
        l3coef = 1.0 - t2 + nu2;
        l4coef = 5.0 - t2 + 9 * nu2 + 4.0 * (nu2 * nu2);
        l5coef = 5.0 - 18.0 * t2 + (t2 * t2) + 14.0 * nu2 - 58.0 * t2 * nu2;
        l6coef = 61.0 - 58.0 * t2 + (t2 * t2) + 270.0 * nu2 - 330.0 * t2 * nu2;
        l7coef = 61.0 - 479.0 * t2 + 179.0 * (t2 * t2) - (t2 * t2 * t2);
        l8coef = 1385.0 - 3111.0 * t2 + 543.0 * (t2 * t2) - (t2 * t2 * t2);
        xy[0] = N * Math.cos (phi) * l + (N / 6.0 * Math.pow (Math.cos (phi), 3.0) * l3coef * Math.pow (l, 3.0)) + (N / 120.0 * Math.pow (Math.cos (phi), 5.0) * l5coef * Math.pow (l, 5.0)) + (N / 5040.0 * Math.pow (Math.cos (phi), 7.0) * l7coef * Math.pow (l, 7.0));
        xy[1] = ArcLengthOfMeridian (phi) + (t / 2.0 * N * Math.pow (Math.cos (phi), 2.0) * Math.pow (l, 2.0)) + (t / 24.0 * N * Math.pow (Math.cos (phi), 4.0) * l4coef * Math.pow (l, 4.0)) + (t / 720.0 * N * Math.pow (Math.cos (phi), 6.0) * l6coef * Math.pow (l, 6.0)) + (t / 40320.0 * N * Math.pow (Math.cos (phi), 8.0) * l8coef * Math.pow (l, 8.0));
    }

    // convertir coordenadas decimales (longitud, latitud) a UTM (x,y)
    private double LatLonToUTMXY (double lat, double lon, double zone, double[] xy){
        MapLatLonToXY (lat, lon, UTMCentralMeridian (zone), xy);

        xy[0] = xy[0] * UTMScaleFactor + 500000.0;
        xy[1] = xy[1] * UTMScaleFactor;
        if (xy[1] < 0.0){
            xy[1] = xy[1] + 10000000.0;
        }
        coordenadasUMT = xy;
        return zone;
    }
}