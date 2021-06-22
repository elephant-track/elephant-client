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
package org.elephant.setting.main;

import static org.elephant.setting.StyleElementsEx.doubleElementEx;
import static org.elephant.setting.StyleElementsEx.stringElement;
import static org.mastodon.app.ui.settings.StyleElements.booleanElement;
import static org.mastodon.app.ui.settings.StyleElements.intElement;
import static org.mastodon.app.ui.settings.StyleElements.label;
import static org.mastodon.app.ui.settings.StyleElements.separator;

import java.util.Arrays;
import java.util.List;

import org.elephant.setting.AbstractElephantSettingsPanel;
import org.mastodon.app.ui.settings.StyleElements.StyleElement;

/**
 * Settings panel for main settings.
 * 
 * @author Ko Sugawara
 */
public class ElephantMainSettingsPanel extends AbstractElephantSettingsPanel< ElephantMainSettings >
{

	private static final long serialVersionUID = 1L;

	public enum MainSettingsMode
	{
		BASIC, ADVANCED
	}

	private final MainSettingsMode settingsMode;

	private final String unit;

	public ElephantMainSettingsPanel( final ElephantMainSettings style )
	{
		this( style, MainSettingsMode.BASIC );
	}

	public ElephantMainSettingsPanel( final ElephantMainSettings style, final MainSettingsMode settingsMode )
	{
		this( style, settingsMode, "unit" );
	}

	public ElephantMainSettingsPanel( final ElephantMainSettings style, final MainSettingsMode settingsMode, final String unit )
	{
		super();
		this.settingsMode = settingsMode;
		this.unit = unit;
		build( style );
	}

	@Override
	protected List< StyleElement > styleElements( final ElephantMainSettings style )
	{
		List< StyleElement > styleElements = null;
		switch ( settingsMode )
		{
		case BASIC:
			styleElements = getBasicStyleElements( style );
			break;
		case ADVANCED:
			styleElements = getAdvancedStyleElements( style );
			break;
		default:
			throw new UnsupportedOperationException( String.format( "%s is not in MainSettingsMode", settingsMode ) );
		}
		return styleElements;
	}

	private List< StyleElement > getBasicStyleElements( final ElephantMainSettings style )
	{
		return Arrays.asList(
				// booleanElement( "debug", style::getDebug, style::setDebug ),
				booleanElement( "prediction with patches", style::getPatch, style::setPatch ),
				intElement( "prediction patch size x", 1, 9999, style::getPatchSizeX, style::setPatchSizeX ),
				intElement( "prediction patch size y", 1, 9999, style::getPatchSizeY, style::setPatchSizeY ),
				intElement( "prediction patch size z", 1, 9999, style::getPatchSizeZ, style::setPatchSizeZ ),

				separator(),

				intElement( "number of crops", 0, 1000, style::getNumCrops, style::setNumCrops ),
				intElement( "number of epochs", 0, 1000, style::getNumEpochs, style::setNumEpochs ),
				intElement( "time range", 1, 100000, style::getTimeRange, style::setTimeRange ),
				doubleElementEx( "auto BG threshold", 0.0, 1, 0.001, style::getAutoBgThreshold, style::setAutoBgThreshold ),
				doubleElementEx( "learning rate", 0.000001, 1, 0.000001, style::getLearningRate, style::setLearningRate ),
				doubleElementEx( "probability threshold", 0.0, 1.0, 0.01, style::getProbThreshold, style::setProbThreshold ),
				doubleElementEx( String.format( "suppression distance (%s)", unit ), 0.0, 100.0, 0.01, style::getSuppressionDistance, style::setSuppressionDistance ),
				doubleElementEx( String.format( "min radius (%s)", unit ), 0.0, 100.0, 0.01, style::getMinRadius, style::setMinRadius ),
				doubleElementEx( String.format( "max radius (%s)", unit ), 0.0, 100.0, 0.01, style::getMaxRadius, style::setMaxRadius ),
				doubleElementEx( String.format( "NN linking threshold (%s)", unit ), 0.0, 100.0, 0.01, style::getNNLinkingThreshold, style::setNNLinkingThreshold ),
				intElement( "NN max edges", 1, 5, style::getNNMaxEdges, style::setNNMaxEdges ),
				booleanElement( "use optical flow for linking", style::getUseOpticalflow, style::setUseOpticalflow ),

				separator(),

				label( "file/dir on the server" ),
				stringElement( "dataset dir (relative path from /workspace/datasets/)", style::getDatasetName, style::setDatasetName ),
				stringElement( "seg model file (relative path from /workspace/models/)", style::getSegModelName, style::setSegModelName ),
				stringElement( "flow model file (relative path from /workspace/models/)", style::getFlowModelName, style::setFlowModelName ),
				stringElement( "seg Tensorboard log dir (relative path from /workspace/logs/)", style::getSegLogName, style::setSegLogName ),
				stringElement( "flow Tensorboard  log dir (relative path from /workspace/logs/)", style::getFlowLogName, style::setFlowLogName ) );
	}

	private List< StyleElement > getAdvancedStyleElements( final ElephantMainSettings style )
	{
		return Arrays.asList(
				booleanElement( "output prediction", style::getOutputPrediction, style::setOutputPrediction ),
				booleanElement( "apply slice-wise median correction", style::getMedianCorrection, style::setMedianCorrection ),
				booleanElement( "mitigate edge discontinuities", style::getPad, style::setPad ),

				separator(),

				intElement( "training crop size x", 1, 9999, style::getTrainingCropSizeX, style::setTrainingCropSizeX ),
				intElement( "training crop size y", 1, 9999, style::getTrainingCropSizeY, style::setTrainingCropSizeY ),
				intElement( "training crop size z", 1, 9999, style::getTrainingCropSizeZ, style::setTrainingCropSizeZ ),

				doubleElementEx( "seg class weight background", 0.0, 100, 0.1, style::getSegWeightBG, style::setSegWeightBG ),
				doubleElementEx( "seg class weight border", 0.0, 100, 0.1, style::getSegWeightBorder, style::setSegWeightBorder ),
				doubleElementEx( "seg class weight center", 0.0, 100, 0.1, style::getSegWeightCenter, style::setSegWeightCenter ),

				doubleElementEx( "flow dim weight x", 0.0, 100, 0.1, style::getFlowWeightX, style::setFlowWeightX ),
				doubleElementEx( "flow dim weight y", 0.0, 100, 0.1, style::getFlowWeightY, style::setFlowWeightY ),
				doubleElementEx( "flow dim weight z", 0.0, 100, 0.1, style::getFlowWeightZ, style::setFlowWeightZ ),

				doubleElementEx( "false weight", 0.0, 100.0, 0.01, style::getFalseWeight, style::setFalseWeight ),
				doubleElementEx( "center ratio", 0.0, 1.0, 0.01, style::getCenterRatio, style::setCenterRatio ),
				doubleElementEx( "max displacement (voxel unit)", 0.0, 1000.0, 1.0, style::getMaxDisplacement, style::setMaxDisplacement ),
				doubleElementEx( "augmentation scale factor base", 0.0, 0.999999, 0.01, style::getAugScaleFactorBase, style::setAugScaleFactorBase ),
				doubleElementEx( "augmentation rotation angle", 0.0, 180.0, 0.01, style::getAugRotationAngle, style::setAugRotationAngle ),
				intElement( "NN search depth", 0, 100, style::getNNSearchDepth, style::setNNSearchDepth ),
				intElement( "NN search neighbors", 0, 100, style::getNNSearchNeighbors, style::setNNSearchNeighbors ),
				booleanElement( "use interpolation for linking", style::getUseInterpolation, style::setUseInterpolation ),
				booleanElement( "use 2d model", style::getUse2dModel, style::setUse2dModel ),

				separator(),

				label( "file on the client" ),
				stringElement( "client log file (relative path from ~/.mastodon/logs/)", style::getLogFileName, style::setLogFileName ) );
	}
}
