package de.voomdoon.tool.map.osmtokml;

import java.util.Map;

import de.voomdoon.util.cli.MainBase;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
public class OsmToKmlMain extends MainBase {

	/**
	 * DOCME add JavaDoc for method main
	 * 
	 * @param args
	 * @since 0.1.0
	 */
	public static void main(String[] args) {
		new OsmToKmlMain(args, Map.of()).run();
	}

	/**
	 * DOCME add JavaDoc for constructor OsmToolMain
	 * 
	 * @param args
	 * @param subMains
	 * @since 0.1.0
	 */
	protected OsmToKmlMain(String[] args, Map<String, Class<?>> subMains) {
		super(args, subMains);
	}

	/**
	 * @since 0.1.0
	 */
	@Override
	protected String getName() {
		return "OSM2KML";
	}
}
