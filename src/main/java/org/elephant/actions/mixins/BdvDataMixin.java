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

import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;

/**
 * Get the metadata of the BDV data.
 * 
 * @author Ko Sugawara
 */
public interface BdvDataMixin extends ElephantActionMixin
{

	default boolean is2D()
	{
		return getAppModel().getSharedBdvData().is2D();
	}

	default Dimensions getDimensions()
	{
		return getAppModel().getSharedBdvData().getSpimData()
				.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getSize();
	}

	default VoxelDimensions getVoxelDimensions()
	{
		return getAppModel().getSharedBdvData().getSpimData()
				.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize();
	}

	default int getNKeepAxials()
	{
		final VoxelDimensions voxelDimensions = getVoxelDimensions();
		final double anisotropy = voxelDimensions.dimension( 2 ) / voxelDimensions.dimension( 0 );
		final int floorLog2 = 31 - Integer.numberOfLeadingZeros( ( int ) anisotropy );
		final int ceilLog2 = 32 - Integer.numberOfLeadingZeros( ( int ) ( anisotropy - 1 ) );
		if ( anisotropy - Math.pow( 2, floorLog2 ) < Math.pow( 2, ceilLog2 ) - anisotropy )
		{
			return floorLog2;
		}
		else
		{
			return ceilLog2;
		}
	}

}
