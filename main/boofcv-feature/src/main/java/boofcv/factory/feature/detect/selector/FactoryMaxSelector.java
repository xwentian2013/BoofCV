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

import boofcv.alg.feature.detect.selector.*;

import javax.annotation.Nullable;

/**
 * Factory that creates {@link FeatureMaxSelector}
 *
 * @author Peter Abeles
 */
public class FactoryMaxSelector {
	public static FeatureMaxSelector create( @Nullable ConfigMaxSelector config ) {
		if( config == null )
			config = new ConfigMaxSelector();
		switch( config.type ) {
			case BEST_N: return new SelectNBestFeatures();
			case RANDOM: return new SelectRandomFeatures(config.randomSeed);
			case UNIFORM_BEST: return new SelectUniformBestFeatures();
			case FIRST: return new SelectFirstFeatures();
		}
		throw new RuntimeException("Unknown type "+config.type);
	}
}
