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

package boofcv.factory.feature.detect.selector;

/**
 * Different types of built in methods for enforcing the maximum allowed number of detected features inside
 * an intensity image.
 *
 * @see boofcv.alg.feature.detect.selector.FeatureMaxSelector
 *
 * @author Peter Abeles
 */
public enum MaxSelectorTypes {
	/**
	 * @see boofcv.alg.feature.detect.selector.SelectUniformBestFeatures
	 */
	BEST_N,
	/**
	 * @see boofcv.alg.feature.detect.selector.SelectUniformBestFeatures
	 */
	UNIFORM_BEST,
	/**
	 * @see boofcv.alg.feature.detect.selector.SelectRandomFeatures
	 */
	RANDOM
}
