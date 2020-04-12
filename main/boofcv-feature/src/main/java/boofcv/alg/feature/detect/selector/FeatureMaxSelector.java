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

import boofcv.struct.image.GrayF32;
import georegression.struct.point.Point2D_I16;
import org.ddogleg.struct.FastAccess;
import org.ddogleg.struct.FastQueue;

/**
 * Resolves ambiguity when the requested number of features is exceeded by the actual number of features detected
 * inside the intensity image.
 *
 * @author Peter Abeles
 */
public interface FeatureMaxSelector {
	/**
	 * Selects features using a rule given the limit on detection objects.
	 * @param intensity (Input) Intensity image
	 * @param positive (Input) true if better features have more positive values, false if it's more negative values
	 * @param prior (Input) Locations of previously detected features
	 * @param detected (Input) Locations of newly detected features
	 * @param limit (Input) The maximum number of new features detected
	 * @param selected (Output) Selected features. Element count not exceed the limit. Reset on every call.
	 */
	void select(GrayF32 intensity , boolean positive, FastAccess<Point2D_I16> prior,
				FastAccess<Point2D_I16> detected, int limit , FastQueue<Point2D_I16> selected );
}
