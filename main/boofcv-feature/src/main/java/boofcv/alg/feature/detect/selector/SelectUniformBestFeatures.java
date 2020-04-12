/*
 * Copyright (c) 2011-2020, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.feature.detect.selector;

import boofcv.misc.BoofMiscOps;
import boofcv.struct.ConfigGridUniform;
import boofcv.struct.ImageGrid;
import boofcv.struct.image.GrayF32;
import georegression.struct.point.Point2D_I16;
import org.ddogleg.sorting.QuickSort_F32;
import org.ddogleg.struct.FastAccess;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_F32;
import org.ddogleg.struct.GrowQueue_I32;

import java.util.ArrayList;
import java.util.List;

/**
 * Attempts to select features uniformally across the image with a preference for locally more intense features. This
 * is done by breaking the image up into a grid. Then features are added by selecting the most intense feature from
 * each grid. This is repeated until the limit has been reached or there are no more features to add.
 *
 * @author Peter Abeles
 */
public class SelectUniformBestFeatures implements FeatureMaxSelector {

	/** Configuration for uniformally selecting a grid */
	public final ConfigGridUniform configUniform = new ConfigGridUniform();

	// Grid for storing a set of objects
	ImageGrid<List<Point2D_I16>> grid = new ImageGrid<>(ArrayList::new,List::clear);

	// Workspace variables for sorting the cells
	GrowQueue_F32 pointIntensity = new GrowQueue_F32();
	QuickSort_F32 sorter = new QuickSort_F32();
	GrowQueue_I32 indexes = new GrowQueue_I32();
	List<Point2D_I16> workList = new ArrayList<>();

	@Override
	public void select(GrayF32 intensity, boolean positive,
					   FastAccess<Point2D_I16> _ignore_, FastAccess<Point2D_I16> detected, int limit,
					   FastQueue<Point2D_I16> selected)
	{
		selected.reset();

		// the limit is more than the total number of features. Return them all!
		if( detected.size <= limit ) {
			BoofMiscOps.copyAll_2D_I16(detected,selected);
			return;
		}

		// Adjust the grid to the requested limit and image shape
		int targetCellSize = configUniform.selectTargetCellSize(limit,intensity.width,intensity.height);
		grid.initialize(targetCellSize,intensity.width,intensity.height);

		// Add all detected points to the grid
		for (int i = 0; i < detected.size; i++) {
			Point2D_I16 p = detected.data[i];
			grid.getCellAtPixel(p.x,p.y).add(p);
		}

		// Sort elements in each cell in order be inverse preference
		sortCellLists(intensity, positive);

		// Add points until the limit has been reached or there are no more cells to add
		final FastAccess<List<Point2D_I16>> cells = grid.cells;
		while( selected.size < limit ) {
			int before = selected.size;
			for (int cellidx = 0; cellidx < cells.size && selected.size < limit; cellidx++) {
				List<Point2D_I16> cellPoints = cells.get(cellidx);
				if (cellPoints.isEmpty())
					continue;
				selected.grow().set( cellPoints.remove( cellPoints.size()-1) );
			}
			if( before == selected.size )
				break;
		}
	}

	/**
	 * Sort points in cells based on their intensity
	 */
	private void sortCellLists(GrayF32 intensity, boolean positive) {
		// Add points to the grid elements and sort them based feature intensity
		final FastAccess<List<Point2D_I16>> cells = grid.cells;
		for (int cellidx = 0; cellidx < cells.size; cellidx++) {
			List<Point2D_I16> cellPoints = cells.get(cellidx);
			if( cellPoints.isEmpty() )
				continue;
			final int N = cellPoints.size();
			pointIntensity.resize(N);
			indexes.resize(N);

			// select the score's sign so that the most desirable is at the end of the list
			// That way elements can be removed from the top of the list, which is less expensive.
			if( positive ) {
				for (int pointIdx = 0; pointIdx < N; pointIdx++) {
					Point2D_I16 p = cellPoints.get(pointIdx);
					pointIntensity.data[pointIdx] = intensity.unsafe_get(p.x, p.y);
				}
			} else {
				for (int pointIdx = 0; pointIdx < N; pointIdx++) {
					Point2D_I16 p = cellPoints.get(pointIdx);
					pointIntensity.data[pointIdx] = -intensity.unsafe_get(p.x, p.y);
				}
			}
			sorter.sort(pointIntensity.data,0,N,indexes.data);

			// Extract an ordered list of points based on intensity and swap out the cell list to avoid a copy
			workList.clear();
			for (int i = 0; i < N; i++) {
				workList.add( cellPoints.get(indexes.data[i]));
			}
			List<Point2D_I16> tmp = cells.data[cellidx];
			cells.data[cellidx] = workList;
			workList = tmp;
		}
	}
}
