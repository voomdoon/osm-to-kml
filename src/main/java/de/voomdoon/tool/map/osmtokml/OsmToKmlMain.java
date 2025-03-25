package de.voomdoon.tool.map.osmtokml;

import de.voomdoon.util.cli.MainBase;
import de.voomdoon.util.cli.Program;

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
		Program.run(args);
	}

	/**
	 * @since 0.1.0
	 */
	@Override
	protected String getName() {
		return "OSM2KML";
	}

	/**
	 * @since 0.1.0
	 */
	@Override
	protected void registerSubMains() {

		// TODO implement registerSubMains
		throw new UnsupportedOperationException("'registerSubMains' not implemented at 'MainBase'!");
	}
}
