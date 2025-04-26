package de.voomdoon.tool.map.osmtokml;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
public class OsmConverter {

	/**
	 * @since 0.1.0
	 */
	private Document document;

	/**
	 * DOCME add JavaDoc for constructor OsmConverter
	 * 
	 * @param document
	 * @since 0.1.0
	 */
	public OsmConverter(Document document) {
		this.document = document;
	}

	/**
	 * DOCME add JavaDoc for method convert
	 * 
	 * @param osmData
	 * @since 0.1.0
	 */
	public void convert(OsmData osmData) {
		Placemark placemark = new Placemark();
		Point point = new Point();
		Node node = osmData.getNodes().entrySet().iterator().next().getValue();
		point.addToCoordinates(node.getLongitude(), node.getLatitude());
		placemark.setGeometry(point);
		document.addToFeature(placemark);
	}
}
