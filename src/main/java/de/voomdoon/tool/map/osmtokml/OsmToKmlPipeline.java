package de.voomdoon.tool.map.osmtokml;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Definition on how to convert OpenStreetMap data to KML.
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class OsmToKmlPipeline {

	/**
	 * @side 0.1.0
	 */
	private String name;
}
