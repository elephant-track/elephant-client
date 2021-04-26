/*******************************************************************************
 * Copyright (C) 2020, Ko Sugawara
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.elephant.actions.mixins;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;

import net.imglib2.Dimensions;

/**
 * A collection of calculations.
 * 
 * @author Ko Sugawara
 */
public interface ElephantGraphActionMixin extends BdvDataMixin, GraphActionMixin
{

	static final int[] DEFAULT_CROP_BOX_SIZE = new int[] { 256, 256, 16 };

	default void calculateCropBoxAround( final double[] pos, final long[] cropOrigin, final long[] cropSize )
	{
		final Dimensions dimensions = getDimensions();
		for ( int i = 0; i < pos.length; i++ )
		{
			cropSize[ i ] = Math.min( dimensions.dimension( i ), DEFAULT_CROP_BOX_SIZE[ i ] );
			pos[ i ] /= getVoxelDimensions().dimension( i );
			pos[ i ] = Math.min(
					dimensions.dimension( i ) - cropSize[ i ],
					Math.max( 0, pos[ i ] - cropSize[ i ] / 2 ) );
			cropOrigin[ i ] = ( long ) pos[ i ];
		}
	}

	default double squaredDistanceOf( final Link edge )
	{
		final double[] posSource = new double[ 3 ];
		final double[] posTarget = new double[ 3 ];
		final Spot ref = getGraph().vertexRef();
		try
		{
			edge.getSource( ref ).localize( posSource );
			edge.getTarget( ref ).localize( posTarget );

		}
		finally
		{
			getGraph().releaseRef( ref );
		}
		double squaredDistance = 0;
		for ( int i = 0; i < 3; i++ )
		{
			final double diff = posSource[ i ] - posTarget[ i ];
			squaredDistance += diff * diff;
		}
		return squaredDistance;
	}

	default double squaredDistanceOf( final Spot spotSource, final Spot spotTarget )
	{
		final double[] posSource = new double[ 3 ];
		final double[] posTarget = new double[ 3 ];
		spotSource.localize( posSource );
		spotTarget.localize( posTarget );
		double squaredDistance = 0;
		for ( int i = 0; i < 3; i++ )
		{
			final double diff = posSource[ i ] - posTarget[ i ];
			squaredDistance += diff * diff;
		}
		return squaredDistance;
	}

}
