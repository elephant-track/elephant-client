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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.elephant.setting.AbstractElephantSettings;

/**
 * Main setting values.
 * 
 * @author Ko Sugawara
 */
public class ElephantMainSettings extends AbstractElephantSettings< ElephantMainSettings >
{

	public static final boolean DEFAULT_IS_DEBUG = false;

	public static final boolean DEFAULT_OUTPUT_PREDICTION = false;

	public static final boolean DEFAULT_USE_MEDIAN_CORRECTION = false;

	public static final boolean DEFAULT_IS_PAD = true;

	public static final boolean DEFAULT_USE_PATCH = false;

	public static final int DEFAULT_PATCH_SIZE_X = 256;

	public static final int DEFAULT_PATCH_SIZE_Y = 256;

	public static final int DEFAULT_PATCH_SIZE_Z = 16;

	public static final int DEFAULT_TRAINING_CROP_SIZE_X = 128;

	public static final int DEFAULT_TRAINING_CROP_SIZE_Y = 128;

	public static final int DEFAULT_TRAINING_CROP_SIZE_Z = 16;

	public static final int DEFAULT_NUM_CROPS = 3;

	public static final int DEFAULT_NUM_EPOCHS = 3;

	public static final int DEFAULT_TIME_RANGE = 10;

	public static final double DEFAULT_AUTO_BG_THRESHOLD = 0.0;

	public static final double DEFAULT_LEARNING_RATE = 0.00005;

	public static final double DEFAULT_SEG_WEIGHT_BG = 1.0;

	public static final double DEFAULT_SEG_WEIGHT_BORDER = 10.0;

	public static final double DEFAULT_SEG_WEIGHT_CENTER = 10.0;

	public static final double DEFAULT_FLOW_WEIGHT_X = 1.0;

	public static final double DEFAULT_FLOW_WEIGHT_Y = 1.0;

	public static final double DEFAULT_FLOW_WEIGHT_Z = 1.0;

	public static final double DEFAULT_FALSE_WEIGHT = 3.0;

	public static final double DEFAULT_CENTER_RATIO = 0.3;

	public static final double DEFAULT_PROB_THRESHOLD = 0.3;

	public static final double DEFAULT_SUPPRESSION_DISTANCE = 5.0;

	public static final double DEFAULT_MIN_RADIUS = 1.0;

	public static final double DEFAULT_MAX_RADIUS = 5.0;

	public static final double DEFAULT_MAX_DISPLACEMENT = 80.0;

	public static final double DEFAULT_AUG_SCALE_FACTOR_BASE = 0.0;

	public static final double DEFAULT_AUG_ROTATION_ANGLE = 0.0;

	public static final double DEFAULT_NN_LINKING_THRESHOLD = 5.0;

	public static final int DEFAULT_NN_SEARCH_DEPTH = 1;

	public static final int DEFAULT_NN_SEARCH_NEIGHBORS = 3;

	public static final int DEFAULT_NN_MAX_EDGES = 2;

	public static final boolean DEFAULT_USE_OPTICALFLOW = false;

	public static final boolean DEFAULT_USE_INTERPOLATION = false;

	public static final boolean DEFAULT_USE_2D_MODEL = false;

	public static final String DEFAULT_SEG_MODEL_NAME = "seg.pth";

	public static final String DEFAULT_FLOW_MODEL_NAME = "flow.pth";

	public static final String DEFAULT_SEG_LOG_NAME = "seg_log";

	public static final String DEFAULT_FLOW_LOG_NAME = "flow_log";

	public static final String DEFAULT_DATASET_NAME = "dataset";

	public static final String DEFAULT_LOG_FILE_NAME = "default.log";

	private ElephantMainSettings()
	{
		super();
	}

	@Override
	protected ElephantMainSettings getNewInstance()
	{
		return new ElephantMainSettings();
	}

	@Override
	public synchronized void set( final ElephantMainSettings settings )
	{
		name = settings.name;
		isDebug = settings.isDebug;
		outputPrediction = settings.outputPrediction;
		useMedianCorrection = settings.useMedianCorrection;
		isPad = settings.isPad;
		usePatch = settings.usePatch;
		patchSizeX = settings.patchSizeX;
		patchSizeY = settings.patchSizeY;
		patchSizeZ = settings.patchSizeZ;
		trainingCropSizeX = settings.trainingCropSizeX;
		trainingCropSizeY = settings.trainingCropSizeY;
		trainingCropSizeZ = settings.trainingCropSizeZ;
		numCrops = settings.numCrops;
		numEpochs = settings.numEpochs;
		timeRange = settings.timeRange;
		autoBgThreshold = settings.autoBgThreshold;
		learningRate = settings.learningRate;
		segWeightBG = settings.segWeightBG;
		segWeightBorder = settings.segWeightBorder;
		segWeightCenter = settings.segWeightCenter;
		flowWeightX = settings.flowWeightX;
		flowWeightY = settings.flowWeightY;
		flowWeightZ = settings.flowWeightZ;
		falseWeight = settings.falseWeight;
		centerRatio = settings.centerRatio;
		probThreshold = settings.probThreshold;
		suppressionDistance = settings.suppressionDistance;
		minRadius = settings.minRadius;
		maxRadius = settings.maxRadius;
		maxDisplacement = settings.maxDisplacement;
		augScaleFactorBase = settings.augScaleFactorBase;
		augRotationAngle = settings.augRotationAngle;
		nnLinkingThreshold = settings.nnLinkingThreshold;
		nnSearchDepth = settings.nnSearchDepth;
		nnSearchNeighbors = settings.nnSearchNeighbors;
		nnMaxEdges = settings.nnMaxEdges;
		useOpticalflow = settings.useOpticalflow;
		useInterpolation = settings.useInterpolation;
		use2dModel = settings.use2dModel;
		segModelName = settings.segModelName;
		flowModelName = settings.flowModelName;
		segLogName = settings.segLogName;
		flowLogName = settings.flowLogName;
		datasetName = settings.datasetName;
		logFileName = settings.logFileName;
		notifyListeners();
	}

	private boolean isDebug = DEFAULT_IS_DEBUG;

	private boolean outputPrediction = DEFAULT_OUTPUT_PREDICTION;

	private boolean useMedianCorrection = DEFAULT_USE_MEDIAN_CORRECTION;

	private boolean isPad = DEFAULT_IS_PAD;

	private boolean usePatch = DEFAULT_USE_PATCH;

	private int patchSizeX = DEFAULT_PATCH_SIZE_X;

	private int patchSizeY = DEFAULT_PATCH_SIZE_Y;

	private int patchSizeZ = DEFAULT_PATCH_SIZE_Z;

	private int trainingCropSizeX = DEFAULT_TRAINING_CROP_SIZE_X;

	private int trainingCropSizeY = DEFAULT_TRAINING_CROP_SIZE_Y;

	private int trainingCropSizeZ = DEFAULT_TRAINING_CROP_SIZE_Z;

	private int numCrops = DEFAULT_NUM_CROPS;

	private int numEpochs = DEFAULT_NUM_EPOCHS;

	private int timeRange = DEFAULT_TIME_RANGE;

	private double autoBgThreshold = DEFAULT_AUTO_BG_THRESHOLD;

	private double learningRate = DEFAULT_LEARNING_RATE;

	private double segWeightBG = DEFAULT_SEG_WEIGHT_BG;

	private double segWeightBorder = DEFAULT_SEG_WEIGHT_BORDER;

	private double segWeightCenter = DEFAULT_SEG_WEIGHT_CENTER;

	private double flowWeightX = DEFAULT_FLOW_WEIGHT_X;

	private double flowWeightY = DEFAULT_FLOW_WEIGHT_Y;

	private double flowWeightZ = DEFAULT_FLOW_WEIGHT_Z;

	private double falseWeight = DEFAULT_FALSE_WEIGHT;

	private double centerRatio = DEFAULT_CENTER_RATIO;

	private double probThreshold = DEFAULT_PROB_THRESHOLD;

	private double suppressionDistance = DEFAULT_SUPPRESSION_DISTANCE;

	private double minRadius = DEFAULT_MIN_RADIUS;

	private double maxRadius = DEFAULT_MAX_RADIUS;

	private double maxDisplacement = DEFAULT_MAX_DISPLACEMENT;

	private double augScaleFactorBase = DEFAULT_AUG_SCALE_FACTOR_BASE;

	private double augRotationAngle = DEFAULT_AUG_ROTATION_ANGLE;

	private double nnLinkingThreshold = DEFAULT_NN_LINKING_THRESHOLD;

	private int nnSearchDepth = DEFAULT_NN_SEARCH_DEPTH;

	private int nnSearchNeighbors = DEFAULT_NN_SEARCH_NEIGHBORS;

	private int nnMaxEdges = DEFAULT_NN_MAX_EDGES;

	private boolean useOpticalflow = DEFAULT_USE_OPTICALFLOW;

	private boolean useInterpolation = DEFAULT_USE_INTERPOLATION;

	private boolean use2dModel = DEFAULT_USE_2D_MODEL;

	private String segModelName = DEFAULT_SEG_MODEL_NAME;

	private String flowModelName = DEFAULT_FLOW_MODEL_NAME;

	private String segLogName = DEFAULT_SEG_LOG_NAME;

	private String flowLogName = DEFAULT_FLOW_LOG_NAME;

	private String datasetName = DEFAULT_DATASET_NAME;

	private String logFileName = DEFAULT_LOG_FILE_NAME;

	public boolean getDebug()
	{
		return isDebug;
	}

	public synchronized void setDebug( final boolean isDebug )
	{
		if ( this.isDebug != isDebug )
		{
			this.isDebug = isDebug;
			notifyListeners();
		}
	}

	public boolean getOutputPrediction()
	{
		return outputPrediction;
	}

	public synchronized void setOutputPrediction( final boolean outputPrediction )
	{
		if ( this.outputPrediction != outputPrediction )
		{
			this.outputPrediction = outputPrediction;
			notifyListeners();
		}
	}

	public boolean getMedianCorrection()
	{
		return useMedianCorrection;
	}

	public synchronized void setMedianCorrection( final boolean useMedianFilter )
	{
		if ( this.useMedianCorrection != useMedianFilter )
		{
			this.useMedianCorrection = useMedianFilter;
			notifyListeners();
		}
	}

	public boolean getPad()
	{
		return isPad;
	}

	public synchronized void setPad( final boolean isPad )
	{
		if ( this.isPad != isPad )
		{
			this.isPad = isPad;
			notifyListeners();
		}
	}

	public boolean getPatch()
	{
		return usePatch;
	}

	public synchronized void setPatch( final boolean usePatch )
	{
		if ( this.usePatch != usePatch )
		{
			this.usePatch = usePatch;
			notifyListeners();
		}
	}

	public int getPatchSizeX()
	{
		return patchSizeX;
	}

	public synchronized void setPatchSizeX( final int patchSizeX )
	{
		if ( this.patchSizeX != patchSizeX )
		{
			this.patchSizeX = patchSizeX;
			notifyListeners();
		}
	}

	public int getPatchSizeY()
	{
		return patchSizeY;
	}

	public synchronized void setPatchSizeY( final int patchSizeY )
	{
		if ( this.patchSizeY != patchSizeY )
		{
			this.patchSizeY = patchSizeY;
			notifyListeners();
		}
	}

	public int getPatchSizeZ()
	{
		return patchSizeZ;
	}

	public synchronized void setPatchSizeZ( final int patchSizeZ )
	{
		if ( this.patchSizeZ != patchSizeZ )
		{
			this.patchSizeZ = patchSizeZ;
			notifyListeners();
		}
	}

	public int getTrainingCropSizeX()
	{
		return trainingCropSizeX;
	}

	public synchronized void setTrainingCropSizeX( final int trainingCropSizeX )
	{
		if ( this.trainingCropSizeX != trainingCropSizeX )
		{
			this.trainingCropSizeX = trainingCropSizeX;
			notifyListeners();
		}
	}

	public int getTrainingCropSizeY()
	{
		return trainingCropSizeY;
	}

	public synchronized void setTrainingCropSizeY( final int trainingCropSizeY )
	{
		if ( this.trainingCropSizeY != trainingCropSizeY )
		{
			this.trainingCropSizeY = trainingCropSizeY;
			notifyListeners();
		}
	}

	public int getTrainingCropSizeZ()
	{
		return trainingCropSizeZ;
	}

	public synchronized void setTrainingCropSizeZ( final int trainingCropSizeZ )
	{
		if ( this.trainingCropSizeZ != trainingCropSizeZ )
		{
			this.trainingCropSizeZ = trainingCropSizeZ;
			notifyListeners();
		}
	}

	public int getNumCrops()
	{
		return numCrops;
	}

	public synchronized void setNumCrops( final int numCrops )
	{
		if ( this.numCrops != numCrops )
		{
			this.numCrops = numCrops;
			notifyListeners();
		}
	}

	public int getNumEpochs()
	{
		return numEpochs;
	}

	public synchronized void setNumEpochs( final int numEpochs )
	{
		if ( this.numEpochs != numEpochs )
		{
			this.numEpochs = numEpochs;
			notifyListeners();
		}
	}

	public int getTimeRange()
	{
		return timeRange;
	}

	public synchronized void setTimeRange( final int timeRange )
	{
		if ( this.timeRange != timeRange )
		{
			this.timeRange = timeRange;
			notifyListeners();
		}
	}

	public double getAutoBgThreshold()
	{
		return autoBgThreshold;
	}

	public synchronized void setAutoBgThreshold( final double autoBgThreshold )
	{
		if ( this.autoBgThreshold != autoBgThreshold )
		{
			this.autoBgThreshold = autoBgThreshold;
			notifyListeners();
		}
	}

	public double getLearningRate()
	{
		return learningRate;
	}

	public synchronized void setLearningRate( final double learningRate )
	{
		if ( this.learningRate != learningRate )
		{
			this.learningRate = learningRate;
			notifyListeners();
		}
	}

	public double getSegWeightBG()
	{
		return segWeightBG;
	}

	public synchronized void setSegWeightBG( final double classWeightBG )
	{
		if ( this.segWeightBG != classWeightBG )
		{
			this.segWeightBG = classWeightBG;
			notifyListeners();
		}
	}

	public double getSegWeightBorder()
	{
		return segWeightBorder;
	}

	public synchronized void setSegWeightBorder( final double classWeightBorder )
	{
		if ( this.segWeightBorder != classWeightBorder )
		{
			this.segWeightBorder = classWeightBorder;
			notifyListeners();
		}
	}

	public double getSegWeightCenter()
	{
		return segWeightCenter;
	}

	public synchronized void setSegWeightCenter( final double classWeightCenter )
	{
		if ( this.segWeightCenter != classWeightCenter )
		{
			this.segWeightCenter = classWeightCenter;
			notifyListeners();
		}
	}

	public double getFlowWeightX()
	{
		return flowWeightX;
	}

	public synchronized void setFlowWeightX( final double flowWeightX )
	{
		if ( this.flowWeightX != flowWeightX )
		{
			this.flowWeightX = flowWeightX;
			notifyListeners();
		}
	}

	public double getFlowWeightY()
	{
		return flowWeightY;
	}

	public synchronized void setFlowWeightY( final double flowWeightY )
	{
		if ( this.flowWeightY != flowWeightY )
		{
			this.flowWeightY = flowWeightY;
			notifyListeners();
		}
	}

	public double getFlowWeightZ()
	{
		return flowWeightZ;
	}

	public synchronized void setFlowWeightZ( final double flowWeightZ )
	{
		if ( this.flowWeightZ != flowWeightZ )
		{
			this.flowWeightZ = flowWeightZ;
			notifyListeners();
		}
	}

	public double getFalseWeight()
	{
		return falseWeight;
	}

	public synchronized void setFalseWeight( final double falseWeight )
	{
		if ( this.falseWeight != falseWeight )
		{
			this.falseWeight = falseWeight;
			notifyListeners();
		}
	}

	public double getCenterRatio()
	{
		return centerRatio;
	}

	public synchronized void setCenterRatio( final double centerRatio )
	{
		if ( this.centerRatio != centerRatio )
		{
			this.centerRatio = centerRatio;
			notifyListeners();
		}
	}

	public double getProbThreshold()
	{
		return probThreshold;
	}

	public synchronized void setProbThreshold( final double probThreshold )
	{
		if ( this.probThreshold != probThreshold )
		{
			this.probThreshold = probThreshold;
			notifyListeners();
		}
	}

	public double getSuppressionDistance()
	{
		return suppressionDistance;
	}

	public synchronized void setSuppressionDistance( final double suppressionDistance )
	{
		if ( this.suppressionDistance != suppressionDistance )
		{
			this.suppressionDistance = suppressionDistance;
			notifyListeners();
		}
	}

	public double getMinRadius()
	{
		return minRadius;
	}

	public synchronized void setMinRadius( final double minRadius )
	{
		if ( this.minRadius != minRadius )
		{
			this.minRadius = minRadius;
			notifyListeners();
		}
	}

	public double getMaxRadius()
	{
		return maxRadius;
	}

	public synchronized void setMaxRadius( final double maxRadius )
	{
		if ( this.maxRadius != maxRadius )
		{
			this.maxRadius = maxRadius;
			notifyListeners();
		}
	}

	public double getMaxDisplacement()
	{
		return maxDisplacement;
	}

	public synchronized void setMaxDisplacement( final double maxDisplacement )
	{
		if ( this.maxDisplacement != maxDisplacement )
		{
			this.maxDisplacement = maxDisplacement;
			notifyListeners();
		}
	}

	public double getAugScaleFactorBase()
	{
		return augScaleFactorBase;
	}

	public synchronized void setAugScaleFactorBase( final double augScaleFactorBase )
	{
		if ( this.augScaleFactorBase != augScaleFactorBase )
		{
			this.augScaleFactorBase = augScaleFactorBase;
			notifyListeners();
		}
	}

	public double getAugRotationAngle()
	{
		return augRotationAngle;
	}

	public synchronized void setAugRotationAngle( final double augRotationAngle )
	{
		if ( this.augRotationAngle != augRotationAngle )
		{
			this.augRotationAngle = augRotationAngle;
			notifyListeners();
		}
	}

	public double getNNLinkingThreshold()
	{
		return nnLinkingThreshold;
	}

	public synchronized void setNNLinkingThreshold( final double nnLinkingThreshold )
	{
		if ( this.nnLinkingThreshold != nnLinkingThreshold )
		{
			this.nnLinkingThreshold = nnLinkingThreshold;
			notifyListeners();
		}
	}

	public int getNNSearchDepth()
	{
		return nnSearchDepth;
	}

	public synchronized void setNNSearchDepth( final int nnSearchDepth )
	{
		if ( this.nnSearchDepth != nnSearchDepth )
		{
			this.nnSearchDepth = nnSearchDepth;
			notifyListeners();
		}
	}

	public int getNNSearchNeighbors()
	{
		return nnSearchNeighbors;
	}

	public synchronized void setNNSearchNeighbors( final int nnSearchNeighbors )
	{
		if ( this.nnSearchNeighbors != nnSearchNeighbors )
		{
			this.nnSearchNeighbors = nnSearchNeighbors;
			notifyListeners();
		}
	}

	public int getNNMaxEdges()
	{
		return nnMaxEdges;
	}

	public synchronized void setNNMaxEdges( final int nnMaxEdges )
	{
		if ( this.nnMaxEdges != nnMaxEdges )
		{
			this.nnMaxEdges = nnMaxEdges;
			notifyListeners();
		}
	}

	public boolean getUseOpticalflow()
	{
		return useOpticalflow;
	}

	public synchronized void setUseOpticalflow( final boolean useOpticalflow )
	{
		if ( this.useOpticalflow != useOpticalflow )
		{
			this.useOpticalflow = useOpticalflow;
			notifyListeners();
		}
	}

	public boolean getUseInterpolation()
	{
		return useInterpolation;
	}

	public synchronized void setUseInterpolation( final boolean useInterpolation )
	{
		if ( this.useInterpolation != useInterpolation )
		{
			this.useInterpolation = useInterpolation;
			notifyListeners();
		}
	}

	public boolean getUse2dModel()
	{
		return use2dModel;
	}

	public synchronized void setUse2dModel( final boolean use2dModel )
	{
		if ( this.use2dModel != use2dModel )
		{
			this.use2dModel = use2dModel;
			notifyListeners();
		}
	}

	public String getSegModelName()
	{
		return segModelName != null ? segModelName : DEFAULT_SEG_MODEL_NAME;
	}

	public synchronized void setSegModelName( final String segModelName )
	{
		if ( !Objects.equals( this.segModelName, segModelName ) )
		{
			this.segModelName = segModelName;
			notifyListeners();
		}
	}

	public String getFlowModelName()
	{
		return flowModelName != null ? flowModelName : DEFAULT_FLOW_MODEL_NAME;
	}

	public synchronized void setFlowModelName( final String flowModelName )
	{
		if ( !Objects.equals( this.flowModelName, flowModelName ) )
		{
			this.flowModelName = flowModelName;
			notifyListeners();
		}
	}

	public String getSegLogName()
	{
		return segLogName != null ? segLogName : DEFAULT_SEG_LOG_NAME;
	}

	public synchronized void setSegLogName( final String segLogName )
	{
		if ( !Objects.equals( this.segLogName, segLogName ) )
		{
			this.segLogName = segLogName;
			notifyListeners();
		}
	}

	public String getFlowLogName()
	{
		return flowLogName != null ? flowLogName : DEFAULT_FLOW_LOG_NAME;
	}

	public synchronized void setFlowLogName( final String flowLogName )
	{
		if ( !Objects.equals( this.flowLogName, flowLogName ) )
		{
			this.flowLogName = flowLogName;
			notifyListeners();
		}
	}

	public String getDatasetName()
	{
		return datasetName != null ? datasetName : DEFAULT_DATASET_NAME;
	}

	public synchronized void setDatasetName( final String datasetName )
	{
		if ( !Objects.equals( this.datasetName, datasetName ) )
		{
			this.datasetName = datasetName;
			notifyListeners();
		}
	}

	public String getLogFileName()
	{
		return logFileName != null ? logFileName : DEFAULT_LOG_FILE_NAME;
	}

	public synchronized void setLogFileName( final String logFileName )
	{
		if ( !Objects.equals( this.logFileName, logFileName ) )
		{
			this.logFileName = logFileName;
			notifyListeners();
		}
	}

	private static final ElephantMainSettings df;
	static
	{
		df = new ElephantMainSettings();
		df.isDebug = DEFAULT_IS_DEBUG;
		df.outputPrediction = DEFAULT_OUTPUT_PREDICTION;
		df.useMedianCorrection = DEFAULT_USE_MEDIAN_CORRECTION;
		df.isPad = DEFAULT_IS_PAD;
		df.usePatch = DEFAULT_USE_PATCH;
		df.patchSizeX = DEFAULT_PATCH_SIZE_X;
		df.patchSizeY = DEFAULT_PATCH_SIZE_Y;
		df.patchSizeZ = DEFAULT_PATCH_SIZE_Z;
		df.trainingCropSizeX = DEFAULT_TRAINING_CROP_SIZE_X;
		df.trainingCropSizeY = DEFAULT_TRAINING_CROP_SIZE_Y;
		df.trainingCropSizeZ = DEFAULT_TRAINING_CROP_SIZE_Z;
		df.numCrops = DEFAULT_NUM_CROPS;
		df.numEpochs = DEFAULT_NUM_EPOCHS;
		df.timeRange = DEFAULT_TIME_RANGE;
		df.autoBgThreshold = DEFAULT_AUTO_BG_THRESHOLD;
		df.learningRate = DEFAULT_LEARNING_RATE;
		df.segWeightBG = DEFAULT_SEG_WEIGHT_BG;
		df.segWeightBorder = DEFAULT_SEG_WEIGHT_BORDER;
		df.segWeightCenter = DEFAULT_SEG_WEIGHT_CENTER;
		df.flowWeightX = DEFAULT_FLOW_WEIGHT_X;
		df.flowWeightY = DEFAULT_FLOW_WEIGHT_Y;
		df.flowWeightZ = DEFAULT_FLOW_WEIGHT_Z;
		df.falseWeight = DEFAULT_FALSE_WEIGHT;
		df.centerRatio = DEFAULT_CENTER_RATIO;
		df.probThreshold = DEFAULT_PROB_THRESHOLD;
		df.suppressionDistance = DEFAULT_SUPPRESSION_DISTANCE;
		df.minRadius = DEFAULT_MIN_RADIUS;
		df.maxRadius = DEFAULT_MAX_RADIUS;
		df.maxDisplacement = DEFAULT_MAX_DISPLACEMENT;
		df.augScaleFactorBase = DEFAULT_AUG_SCALE_FACTOR_BASE;
		df.augRotationAngle = DEFAULT_AUG_ROTATION_ANGLE;
		df.nnLinkingThreshold = DEFAULT_NN_LINKING_THRESHOLD;
		df.nnSearchDepth = DEFAULT_NN_SEARCH_DEPTH;
		df.nnSearchNeighbors = DEFAULT_NN_SEARCH_NEIGHBORS;
		df.nnMaxEdges = DEFAULT_NN_MAX_EDGES;
		df.useOpticalflow = DEFAULT_USE_OPTICALFLOW;
		df.useInterpolation = DEFAULT_USE_INTERPOLATION;
		df.use2dModel = DEFAULT_USE_2D_MODEL;
		df.segModelName = DEFAULT_SEG_MODEL_NAME;
		df.flowModelName = DEFAULT_FLOW_MODEL_NAME;
		df.segLogName = DEFAULT_SEG_LOG_NAME;
		df.flowLogName = DEFAULT_FLOW_LOG_NAME;
		df.datasetName = DEFAULT_DATASET_NAME;
		df.logFileName = DEFAULT_LOG_FILE_NAME;
		df.name = "Default";
	}

	public static final Collection< ElephantMainSettings > defaults;
	static
	{
		defaults = new ArrayList<>( 2 );
		defaults.add( df );
	}

	public static ElephantMainSettings defaultStyle()
	{
		return df;
	}
}
