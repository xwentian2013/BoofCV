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

package boofcv.alg.feature.detect.extract;

import boofcv.concurrency.BoofConcurrency;
import boofcv.struct.QueueCorner;
import boofcv.struct.image.GrayF32;
import georegression.struct.point.Point2D_I16;
import org.ddogleg.struct.FastAccess;
import org.ddogleg.struct.FastQueue;

/**
 * Concurrent implementation of {@link NonMaxCandidate}.
 *
 *
 * @author Peter Abeles
 */
public class NonMaxCandidate_MT extends NonMaxCandidate {

	final FastQueue<SearchData> searches = new FastQueue<>(this::createSearchData);

	public NonMaxCandidate_MT(Search search) {
		super(search);
	}

	@Override
	protected void examineMinimum(GrayF32 intensityImage , FastAccess<Point2D_I16> candidates , FastQueue<Point2D_I16> found ) {
		found.reset();
		final int stride = intensityImage.stride;
		final float inten[] = intensityImage.data;

		// little cost to creating a thread so let it select the minimum block size
		BoofConcurrency.loopBlocks(0,candidates.size,searches,(blockData,idx0,idx1)->{
			final QueueCorner threadCorners = blockData.corners;
			final NonMaxCandidate.Search search = blockData.search;

			threadCorners.reset();
			search.initialize(intensityImage);

			for (int iter = idx0; iter < idx1; iter++) {
				Point2D_I16 pt = candidates.data[iter];

				if( pt.x < ignoreBorder || pt.y < ignoreBorder || pt.x >= endBorderX || pt.y >= endBorderY)
					continue;

				int center = intensityImage.startIndex + pt.y * stride + pt.x;

				float val = inten[center];
				if (val > thresholdMin || val == -Float.MAX_VALUE ) continue;

				int x0 = Math.max(0,pt.x - radius);
				int y0 = Math.max(0,pt.y - radius);
				int x1 = Math.min(intensityImage.width, pt.x + radius + 1);
				int y1 = Math.min(intensityImage.height, pt.y + radius + 1);

				if( search.searchMin(x0,y0,x1,y1,center,val) )
					threadCorners.add(pt.x,pt.y);
			}
		});

		// by doing the last step outside we ensure the corners are in a deterministic order and that no locking
		// is required inside each thread
		for (int i = 0; i < searches.size; i++) {
			found.copyAll(searches.get(i).corners.toList(),(src,dst)->dst.set(src));
		}
	}

	@Override
	protected void examineMaximum(GrayF32 intensityImage , FastAccess<Point2D_I16> candidates , FastQueue<Point2D_I16> found ) {
		found.reset();
		final int stride = intensityImage.stride;
		final float inten[] = intensityImage.data;

		// little cost to creating a thread so let it select the minimum block size
		BoofConcurrency.loopBlocks(0,candidates.size,searches,(blockData,idx0,idx1)-> {
			final QueueCorner threadCorners = blockData.corners;
			final NonMaxCandidate.Search search = blockData.search;

			threadCorners.reset();
			search.initialize(intensityImage);

			for (int iter = idx0; iter < idx1; iter++) {
				Point2D_I16 pt = candidates.data[iter];

				if (pt.x < ignoreBorder || pt.y < ignoreBorder || pt.x >= endBorderX || pt.y >= endBorderY)
					continue;

				int center = intensityImage.startIndex + pt.y * stride + pt.x;

				float val = inten[center];
				if (val < thresholdMax || val == Float.MAX_VALUE) continue;

				int x0 = Math.max(0, pt.x - radius);
				int y0 = Math.max(0, pt.y - radius);
				int x1 = Math.min(intensityImage.width, pt.x + radius + 1);
				int y1 = Math.min(intensityImage.height, pt.y + radius + 1);

				if (search.searchMax(x0, y0, x1, y1, center, val))
					threadCorners.add(pt.x, pt.y);
			}
		});

		// by doing the last step outside we ensure the corners are in a deterministic order and that no locking
		// is required inside each thread
		for (int i = 0; i < searches.size; i++) {
			found.copyAll(searches.get(i).corners.toList(),(src,dst)->dst.set(src));
		}
	}

	public SearchData createSearchData() {
		return new SearchData(search.newInstance());
	}

	protected static class SearchData {
		public final Search search;
		public final QueueCorner corners = new QueueCorner();

		public SearchData(Search search) {
			this.search = search;
		}
	}

}
