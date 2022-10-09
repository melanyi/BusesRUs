package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.RoutePattern;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /**
     * overlay used to display bus route legend text on a layer above the map
     */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /**
     * overlays used to plot bus routes
     */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     *
     * @param context the application context
     * @param mapView the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */

    public void plotRoutes(int zoomLevel) {
        Stop selected = StopManager.getInstance().getSelected();
        updateVisibleArea();

        if (selected != null) {
            busRouteLegendOverlay.clear();
            busRouteOverlays.clear();
            for (Route r : StopManager.getInstance().getSelected().getRoutes()) {
                busRouteLegendOverlay.add(r.getNumber());
                for (RoutePattern rp : r.getPatterns()) {
                    LatLon src = null;
                    plotLines(rp, src, r, zoomLevel);
                }
            }
        }
    }


    /**
     *  for the given route pattern, plot the lines between the two given points
     *
     * @param rp the given route pattern
     * @param src the source point
     * @param r the given route
     * @param zoomLevel the given zoom level
     */
    private void plotLines(RoutePattern rp, LatLon src, Route r, int zoomLevel) {
        for (LatLon latLon : rp.getPath()) {
            if (src != null) {
                LatLon dst = new LatLon(latLon.getLatitude(), latLon.getLongitude());
                if (dst != null) {
                    if (Geometry.rectangleIntersectsLine(northWest, southEast, src, dst)) {
                        plotRoute(zoomLevel, src, dst, r);
                    }
                    src = dst;
                }
            } else {
                src = new LatLon(latLon.getLatitude(), latLon.getLongitude());
            }
        }
    }


    /** Plot visible segment of given route pattern going through the selected stop.
     *
     * @param zoomLevel the given zoom level
     * @param src the given source point
     * @param dst the given destination point
     * @param r the route
     */
    private void plotRoute(int zoomLevel, LatLon src, LatLon dst, Route r) {
        List<GeoPoint> geoPoints = new ArrayList<>();

        Polyline polyline = new Polyline(mapView.getContext());
        polyline.setColor(busRouteLegendOverlay.getColor(r.getNumber()));
        geoPoints.add(new GeoPoint(Geometry.gpFromLatLon(src)));
        geoPoints.add(new GeoPoint(Geometry.gpFromLatLon(dst)));
        polyline.setPoints(geoPoints);
        polyline.setVisible(true);
        polyline.setWidth(getLineWidth(zoomLevel));
        busRouteOverlays.add(polyline);
    }

    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     *
     * @param zoomLevel the zoom level of the map
     * @return width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if (zoomLevel > 14) {
            return 7.0f * BusesAreUs.dpiFactor();
        } else if (zoomLevel > 10) {
            return 5.0f * BusesAreUs.dpiFactor();
        } else {
            return 2.0f * BusesAreUs.dpiFactor();
        }
    }
}
