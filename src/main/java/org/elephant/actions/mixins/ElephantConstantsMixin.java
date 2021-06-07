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

/**
 * Provide constants that are used throughout the ELEPHANT plugin.
 * 
 * @author Ko Sugawara
 */
public interface ElephantConstantsMixin
{

	/**
	 * Tag and TagSet
	 */

	public static final String NO_TAG = "";

	public static final String DETECTION_TAGSET_NAME = "Detection";

	public static final String DETECTION_TP_TAG_NAME = "tp";

	public static final String DETECTION_FP_TAG_NAME = "fp";

	public static final String DETECTION_TN_TAG_NAME = "tn";

	public static final String DETECTION_FN_TAG_NAME = "fn";

	public static final String DETECTION_TB_TAG_NAME = "tb";

	public static final String DETECTION_FB_TAG_NAME = "fb";

	public static final String DETECTION_UNLABELED_TAG_NAME = "unlabeled";

	public static final String TRACKING_TAGSET_NAME = "Tracking";

	public static final String TRACKING_APPROVED_TAG_NAME = "Approved";

	public static final String TRACKING_UNLABELED_TAG_NAME = "unlabeled";

	public static final String PROGENITOR_TAGSET_NAME = "Progenitor";

	public static final String PROGENITOR_UNLABELED_TAG_NAME = "unlabeled";

	public static final String STATUS_TAGSET_NAME = "Status";

	public static final String STATUS_COMPLETED_TAG_NAME = "Completed";

	public static final String PROLIFERATOR_TAGSET_NAME = "Proliferator";

	public static final String PROLIFERATOR_PROLIFERATOR_TAG_NAME = "Proliferator";

	public static final String PROLIFERATOR_NONPROLIFERATOR_TAG_NAME = "Non-proliferator";

	public static final String PROLIFERATOR_INVISIBLE_TAG_NAME = "Invisible";

	public static final String DIVISION_TAGSET_NAME = "Division";

	public static final String DIVISION_DIVIDING_TAG_NAME = "Dividing";

	public static final String DIVISION_DIVIDED_TAG_NAME = "Divided";

	public static final String DIVISION_NONDIVIDING_TAG_NAME = "Non-dividing";

	public static final String DIVISION_INVISIBLE_TAG_NAME = "Invisible";

	/**
	 * REST API endpoints
	 */

	public static final String ENDPOINT_STATE = "state";

	public static final String ENDPOINT_PARAMS = "params";

	public static final String ENDPOINT_PREDICT_SEG = "predict/seg";

	public static final String ENDPOINT_PREDICT_FLOW = "predict/flow";

	public static final String ENDPOINT_TRAIN_SEG = "train/seg";

	public static final String ENDPOINT_TRAIN_FLOW = "train/flow";

	public static final String ENDPOINT_UPDATE_SEG = "update/seg";

	public static final String ENDPOINT_UPDATE_FLOW = "update/flow";

	public static final String ENDPOINT_RESET_SEG_MODEL = "reset/seg";

	public static final String ENDPOINT_RESET_FLOW_MODEL = "reset/flow";

	public static final String ENDPOINT_EXPORT_CTC = "export/ctc";

	/**
	 * REST API JSON keys
	 */

	public static final String JSON_KEY_STATE = "state";

	public static final String JSON_KEY_DATASET_NAME = "dataset_name";

	public static final String JSON_KEY_SEG_MODEL_NAME = "model_name";

	public static final String JSON_KEY_FLOW_MODEL_NAME = "model_name";

	public static final String JSON_KEY_SEG_LOG_DIR = "log_dir";

	public static final String JSON_KEY_FLOW_LOG_DIR = "log_dir";

	public static final String JSON_KEY_DEBUG = "debug";

	public static final String JSON_KEY_SCALES = "scales";

	public static final String JSON_KEY_TRAIN_CROP_SIZE = "crop_size";

	public static final String JSON_KEY_N_KEEP_AXIALS = "n_keep_axials";

	public static final String JSON_KEY_IS_3D = "is_3d";

	public static final String JSON_KEY_SPOTS = "spots";

	public static final String JSON_KEY_T_START = "t_start";

	public static final String JSON_KEY_T_END = "t_end";

	public static final String JSON_KEY_OUTPUT_PREDICTION = "output_prediction";

	public static final String JSON_KEY_TIMEPOINT = "timepoint";

	public static final String JSON_KEY_PATCH = "patch";

	public static final String JSON_KEY_PREDICT_CROP_BOX = "crop_box";

	public static final String JSON_KEY_C_RATIO = "c_ratio";

	public static final String JSON_KEY_P_THRESH = "p_thresh";

	public static final String JSON_KEY_R_MIN = "r_min";

	public static final String JSON_KEY_R_MAX = "r_max";

	public static final String JSON_KEY_MAX_DISPLACEMENT = "max_displacement";

	public static final String JSON_KEY_RESET = "reset";

	public static final String JSON_KEY_N_CROPS = "n_crops";

	public static final String JSON_KEY_N_EPOCHS = "n_epochs";

	public static final String JSON_KEY_LR = "lr";

	public static final String JSON_KEY_AUG_SCALE_FACTOR_BASE = "aug_scale_factor_base";

	public static final String JSON_KEY_AUG_ROTATION_ANGLE = "aug_rotation_angle";

	public static final String JSON_KEY_IS_LIVEMODE = "is_livemode";

	public static final String JSON_KEY_SEG_CLASS_WEIGHTS = "class_weights";

	public static final String JSON_KEY_FLOW_DIM_WEIGHTS = "dim_weights";

	public static final String JSON_KEY_FALSE_WEIGHT = "false_weight";

	public static final String JSON_KEY_AUTO_BG_THRESH = "auto_bg_thresh";

	public static final String JSON_KEY_USE_MEDIAN = "use_median";

	public static final String JSON_KEY_IS_PAD = "is_pad";

}
