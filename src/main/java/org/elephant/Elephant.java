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
package org.elephant;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.elephant.actions.AbortProcessingAction;
import org.elephant.actions.AbstractElephantAction;
import org.elephant.actions.BackTrackAction;
import org.elephant.actions.BdvColoringService;
import org.elephant.actions.BdvContextService;
import org.elephant.actions.BdvViewMouseMotionService;
import org.elephant.actions.ChangeDetectionTagSetColorsAction;
import org.elephant.actions.ChangeEllipsoidSizeAction;
import org.elephant.actions.ChangeEllipsoidSizeAction.ChangeEllipsoidSizeActionMode;
import org.elephant.actions.CountDivisionsAction;
import org.elephant.actions.CountDivisionsAction.CountDivisionsActionMode;
import org.elephant.actions.DownloadModelAction;
import org.elephant.actions.DownloadModelAction.DownloadModelActionMode;
import org.elephant.actions.ElephantActionStateManager;
import org.elephant.actions.ElephantOverlayService;
import org.elephant.actions.ElephantServerStatusListener;
import org.elephant.actions.ElephantStatusService;
import org.elephant.actions.ElephantUndoActions;
import org.elephant.actions.ExportCTCAction;
import org.elephant.actions.GraphListenerService;
import org.elephant.actions.HighlightListenerService;
import org.elephant.actions.ImportMastodonAction;
import org.elephant.actions.LoggerService;
import org.elephant.actions.MapTagAction;
import org.elephant.actions.MapTagAction.ChangeTagActionMode;
import org.elephant.actions.NearestNeighborLinkingAction;
import org.elephant.actions.NearestNeighborLinkingAction.NearestNeighborLinkingActionMode;
import org.elephant.actions.PredictSpotsAction;
import org.elephant.actions.PredictSpotsAction.PredictSpotsActionMode;
import org.elephant.actions.RabbitMQDatasetListener;
import org.elephant.actions.RabbitMQService;
import org.elephant.actions.RabbitMQStatusListener;
import org.elephant.actions.RecordSnapshotMovieAction;
import org.elephant.actions.RemoveAllAction;
import org.elephant.actions.RemoveLinksByTagAction;
import org.elephant.actions.RemoveSelfLinksAction;
import org.elephant.actions.RemoveShortTracksAction;
import org.elephant.actions.RemoveSpotsByTagAction;
import org.elephant.actions.RemoveVisibleSpotsAction;
import org.elephant.actions.ResetDetectionLabelsAction;
import org.elephant.actions.ResetDetectionModelAction;
import org.elephant.actions.ResetEllipsoidRotation;
import org.elephant.actions.ResetFlowLabelsAction;
import org.elephant.actions.ResetFlowModelAction;
import org.elephant.actions.RotateEllipsoidAction;
import org.elephant.actions.RotateEllipsoidAction.RotateEllipsoidActionMode;
import org.elephant.actions.SetControlAxisAction;
import org.elephant.actions.SetControlAxisAction.ControlAxis;
import org.elephant.actions.SetUpTagSetsService;
import org.elephant.actions.ShowControlPanelAction;
import org.elephant.actions.ShowLogWindowAction;
import org.elephant.actions.ShowPreferencesAction;
import org.elephant.actions.TagDividingCellAction;
import org.elephant.actions.TagHighlightedVertexAction;
import org.elephant.actions.TagHighlightedVertexAction.TagMode;
import org.elephant.actions.TagProgenitorAction;
import org.elephant.actions.TagProliferatorAction;
import org.elephant.actions.TakeSnapshotAction;
import org.elephant.actions.TrainDetectionAction;
import org.elephant.actions.TrainDetectionAction.TrainingMode;
import org.elephant.actions.TrainFlowAction;
import org.elephant.actions.UnirestService;
import org.elephant.actions.UpdateDetectionLabelsAction;
import org.elephant.actions.UpdateFlowLabelsAction;
import org.elephant.actions.UpdateTrainingParametersService;
import org.elephant.actions.UploadAction;
import org.elephant.actions.VertexPositionListenerService;
import org.mastodon.app.plugin.MastodonPlugin;
import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectCreator;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import bdv.ui.keymap.Keymap.UpdateListener;
import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.command.ContextCommand;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.behaviour.util.Actions;

import mpicbg.spim.data.SpimDataException;

/**
 * An implementation of {@link MastodonPlugin} that initializes and organizes
 * Actions.
 * 
 * @author Ko Sugawara
 */
@Plugin( type = MamutPlugin.class )
public class Elephant extends AbstractContextual implements MamutPlugin, UpdateListener
{

	private ProjectModel pluginAppModel;

	private GroupHandle groupHandle;

	private final AbstractElephantAction backTrackAction;

	private final AbstractElephantAction predictSpotsAction;

	private final AbstractElephantAction updateDetectionLabelsAction;

	private final AbstractElephantAction updateFlowLabelsAction;

	private final AbstractElephantAction liveTrainingAction;

	private final AbstractElephantAction trainSelectedAction;

	private final AbstractElephantAction trainAllAction;

	private final AbstractElephantAction abortProcessingAction;

	private final AbstractElephantAction nnLinkingAction;

	private final AbstractElephantAction trainFlowAction;

	private final AbstractElephantAction showControlPanelAction;

	private final AbstractElephantAction showPreferencesAction;

	private final AbstractElephantAction resetDetectionModelAction;

	private final AbstractElephantAction resetFlowModelAction;

	private final AbstractElephantAction resetDetectionLabelsAction;

	private final AbstractElephantAction resetFlowLabelsAction;

	private final AbstractElephantAction mapSpotTagAction;

	private final AbstractElephantAction mapLinkTagAction;

	private final AbstractElephantAction removeShortTracksAction;

	private final AbstractElephantAction removeSpotsByTagAction;

	private final AbstractElephantAction removeEdgesByTagAction;

	private final AbstractElephantAction removeVisibleSpotsAction;

	private final AbstractElephantAction removeSelfLinksAction;

	private final AbstractElephantAction removeAllAction;

	private final AbstractElephantAction tagHighlightedVerexWitTpAction;

	private final AbstractElephantAction tagHighlightedVerexWitFpAction;

	private final AbstractElephantAction tagHighlightedVerexWitTnAction;

	private final AbstractElephantAction tagHighlightedVerexWitFnAction;

	private final AbstractElephantAction tagHighlightedVerexWitTbAction;

	private final AbstractElephantAction tagHighlightedVerexWitFbAction;

	private final AbstractElephantAction tagHighlightedVerexWitUnlabeledAction;

	private final AbstractElephantAction setControlAxisXAction;

	private final AbstractElephantAction setControlAxisYAction;

	private final AbstractElephantAction setControlAxisZAction;

	private final AbstractElephantAction increaseEllipsoidSizeAction;

	private final AbstractElephantAction decreaseEllipsoidSizeAction;

	private final AbstractElephantAction rotateEllipsoidClockwiseAction;

	private final AbstractElephantAction rotateEllipsoidCounterclockwiseAction;

	private final AbstractElephantAction resetEllipsoidRotationAction;

	private final AbstractElephantAction countDivisionsEntireAction;

	private final AbstractElephantAction countDivisionsTrackwiseAction;

	private final AbstractElephantAction tagProgenitorAction;

	private final AbstractElephantAction tagProliferatorAction;

	private final AbstractElephantAction tagDividingCellsAction;

	private final AbstractElephantAction takeSnapshotAction;

	private final AbstractElephantAction recordSnapshotMovieAction;

	private final AbstractElephantAction importMastodonAction;

	private final AbstractElephantAction exportCTCAction;

	private final AbstractElephantAction downloadDetectionModelAction;

	private final AbstractElephantAction downloadFlowModelAction;

	private final AbstractElephantAction showLogWindowAction;

	private final AbstractElephantAction changeDetectionTagSetColorsAction;

	private final AbstractElephantAction uploadAction;

	private final List< AbstractElephantAction > pluginActions = new ArrayList<>();

	private final BdvViewMouseMotionService mouseMotionService;

	private final BdvContextService bdvContextService;

	private final ElephantStatusService elephantStatusService;

	final LoggerService loggerService = new LoggerService();

	@Parameter
	private LogService scijavaLogService;

	@Parameter
	private UIService uiService;

	public Elephant()
	{
		loggerService.setup();
		mouseMotionService = new BdvViewMouseMotionService();
		bdvContextService = new BdvContextService();
		elephantStatusService = new ElephantStatusService();
		backTrackAction = new BackTrackAction();
		pluginActions.add( backTrackAction );
		predictSpotsAction = new PredictSpotsAction( PredictSpotsActionMode.ENTIRE, mouseMotionService );
		pluginActions.add( predictSpotsAction );
		pluginActions.add( new PredictSpotsAction( PredictSpotsActionMode.AROUND_MOUSE, mouseMotionService ) );
		updateDetectionLabelsAction = new UpdateDetectionLabelsAction( bdvContextService );
		pluginActions.add( updateDetectionLabelsAction );
		updateFlowLabelsAction = new UpdateFlowLabelsAction();
		pluginActions.add( updateFlowLabelsAction );
		liveTrainingAction = new TrainDetectionAction( TrainingMode.LIVE, bdvContextService );
		pluginActions.add( liveTrainingAction );
		trainSelectedAction = new TrainDetectionAction( TrainingMode.SELECTED, bdvContextService );
		pluginActions.add( trainSelectedAction );
		trainAllAction = new TrainDetectionAction( TrainingMode.ALL, bdvContextService );
		pluginActions.add( trainAllAction );
		resetDetectionModelAction = new ResetDetectionModelAction();
		pluginActions.add( resetDetectionModelAction );
		resetFlowModelAction = new ResetFlowModelAction();
		pluginActions.add( resetFlowModelAction );
		resetDetectionLabelsAction = new ResetDetectionLabelsAction();
		pluginActions.add( resetDetectionLabelsAction );
		resetFlowLabelsAction = new ResetFlowLabelsAction();
		pluginActions.add( resetFlowLabelsAction );
		nnLinkingAction = new NearestNeighborLinkingAction( NearestNeighborLinkingActionMode.ENTIRE, mouseMotionService );
		pluginActions.add( nnLinkingAction );
		pluginActions.add( new NearestNeighborLinkingAction( NearestNeighborLinkingActionMode.AROUND_MOUSE, mouseMotionService ) );
		trainFlowAction = new TrainFlowAction();
		pluginActions.add( trainFlowAction );
		abortProcessingAction = new AbortProcessingAction();
		pluginActions.add( abortProcessingAction );
		showLogWindowAction = new ShowLogWindowAction();
		pluginActions.add( showLogWindowAction );
		showControlPanelAction = new ShowControlPanelAction();
		pluginActions.add( showControlPanelAction );
		showPreferencesAction = new ShowPreferencesAction();
		pluginActions.add( showPreferencesAction );
		mapSpotTagAction = new MapTagAction( ChangeTagActionMode.SPOT );
		pluginActions.add( mapSpotTagAction );
		mapLinkTagAction = new MapTagAction( ChangeTagActionMode.LINK );
		pluginActions.add( mapLinkTagAction );
		removeShortTracksAction = new RemoveShortTracksAction();
		pluginActions.add( removeShortTracksAction );
		removeSpotsByTagAction = new RemoveSpotsByTagAction();
		pluginActions.add( removeSpotsByTagAction );
		removeEdgesByTagAction = new RemoveLinksByTagAction();
		pluginActions.add( removeEdgesByTagAction );
		removeVisibleSpotsAction = new RemoveVisibleSpotsAction( bdvContextService );
		pluginActions.add( removeVisibleSpotsAction );
		removeSelfLinksAction = new RemoveSelfLinksAction();
		pluginActions.add( removeSelfLinksAction );
		removeAllAction = new RemoveAllAction();
		pluginActions.add( removeAllAction );
		tagHighlightedVerexWitTpAction = new TagHighlightedVertexAction( TagMode.TP );
		pluginActions.add( tagHighlightedVerexWitTpAction );
		tagHighlightedVerexWitFpAction = new TagHighlightedVertexAction( TagMode.FP );
		pluginActions.add( tagHighlightedVerexWitFpAction );
		tagHighlightedVerexWitTnAction = new TagHighlightedVertexAction( TagMode.TN );
		pluginActions.add( tagHighlightedVerexWitTnAction );
		tagHighlightedVerexWitFnAction = new TagHighlightedVertexAction( TagMode.FN );
		pluginActions.add( tagHighlightedVerexWitFnAction );
		tagHighlightedVerexWitTbAction = new TagHighlightedVertexAction( TagMode.TB );
		pluginActions.add( tagHighlightedVerexWitTbAction );
		tagHighlightedVerexWitFbAction = new TagHighlightedVertexAction( TagMode.FB );
		pluginActions.add( tagHighlightedVerexWitFbAction );
		tagHighlightedVerexWitUnlabeledAction = new TagHighlightedVertexAction( TagMode.UNLABELED );
		pluginActions.add( tagHighlightedVerexWitUnlabeledAction );
		setControlAxisXAction = new SetControlAxisAction( ControlAxis.X );
		pluginActions.add( setControlAxisXAction );
		setControlAxisYAction = new SetControlAxisAction( ControlAxis.Y );
		pluginActions.add( setControlAxisYAction );
		setControlAxisZAction = new SetControlAxisAction( ControlAxis.Z );
		pluginActions.add( setControlAxisZAction );
		increaseEllipsoidSizeAction = new ChangeEllipsoidSizeAction( ChangeEllipsoidSizeActionMode.INCREASE );
		pluginActions.add( increaseEllipsoidSizeAction );
		decreaseEllipsoidSizeAction = new ChangeEllipsoidSizeAction( ChangeEllipsoidSizeActionMode.DECREASE );
		pluginActions.add( decreaseEllipsoidSizeAction );
		rotateEllipsoidClockwiseAction = new RotateEllipsoidAction( RotateEllipsoidActionMode.CLOCKWISE );
		pluginActions.add( rotateEllipsoidClockwiseAction );
		rotateEllipsoidCounterclockwiseAction = new RotateEllipsoidAction( RotateEllipsoidActionMode.COUNTERCLOCKWISE );
		pluginActions.add( rotateEllipsoidCounterclockwiseAction );
		resetEllipsoidRotationAction = new ResetEllipsoidRotation();
		pluginActions.add( resetEllipsoidRotationAction );
		countDivisionsEntireAction = new CountDivisionsAction( CountDivisionsActionMode.ENTIRE );
		pluginActions.add( countDivisionsEntireAction );
		countDivisionsTrackwiseAction = new CountDivisionsAction( CountDivisionsActionMode.TRACKWISE );
		pluginActions.add( countDivisionsTrackwiseAction );
		tagProgenitorAction = new TagProgenitorAction();
		pluginActions.add( tagProgenitorAction );
		tagProliferatorAction = new TagProliferatorAction();
		pluginActions.add( tagProliferatorAction );
		tagDividingCellsAction = new TagDividingCellAction();
		pluginActions.add( tagDividingCellsAction );
		takeSnapshotAction = new TakeSnapshotAction();
		pluginActions.add( takeSnapshotAction );
		recordSnapshotMovieAction = new RecordSnapshotMovieAction();
		pluginActions.add( recordSnapshotMovieAction );
		importMastodonAction = new ImportMastodonAction();
		pluginActions.add( importMastodonAction );
		exportCTCAction = new ExportCTCAction();
		pluginActions.add( exportCTCAction );
		downloadDetectionModelAction = new DownloadModelAction( DownloadModelActionMode.DETECTION );
		pluginActions.add( downloadDetectionModelAction );
		downloadFlowModelAction = new DownloadModelAction( DownloadModelActionMode.FLOW );
		pluginActions.add( downloadFlowModelAction );
		changeDetectionTagSetColorsAction = new ChangeDetectionTagSetColorsAction();
		pluginActions.add( changeDetectionTagSetColorsAction );
		uploadAction = new UploadAction();
		pluginActions.add( uploadAction );
	}

	/**
	 * Set up {@link MastodonPluginAppModel}-dependent modules.
	 */
	@Override
	public void setAppPluginModel( final ProjectModel pluginAppModel )
	{
		loggerService.setupSciJavaHandler( scijavaLogService );
		( ( ShowLogWindowAction ) showLogWindowAction ).setUIService( uiService );

		this.pluginAppModel = pluginAppModel;
		// Create a GroupHandle instance
		groupHandle = pluginAppModel.getGroupManager().createGroupHandle();
		groupHandle.setGroupId( 0 );
		// Initialize actions
		for ( final AbstractElephantAction pluginAction : pluginActions )
		{
			pluginAction.init( pluginAppModel, groupHandle );
		}

		// Overwrite Undo/Redo app actions
		ElephantUndoActions.installOverwrite( pluginAppModel.getModelActions(), pluginAppModel.getModel() );

		// Initialize MastodonPluginAppModel-dependent services
		// BdvViewMouseMotionService
		mouseMotionService.init( pluginAppModel );
		// BdvColoringService
		new BdvColoringService().init( pluginAppModel );
		// BdvContextService
		bdvContextService.init( pluginAppModel );
		// ElephantStatusService
		elephantStatusService.elephantServerStatusListeners().add( ( ElephantServerStatusListener ) showControlPanelAction );
		elephantStatusService.init( pluginAppModel );
		elephantStatusService.start();
		// ElephantOverlayService
		new ElephantOverlayService( pluginAppModel );
		// RabbitMQService
		final RabbitMQService rabbitMQService = new RabbitMQService();
		rabbitMQService.init( pluginAppModel );
		rabbitMQService.rabbitMQStatusListeners().add( ( RabbitMQStatusListener ) showControlPanelAction );
		rabbitMQService.rabbitMQDatasetListeners().addAll(
				pluginActions.stream()
						.filter( action -> action instanceof RabbitMQDatasetListener )
						.map( action -> ( RabbitMQDatasetListener ) action )
						.collect( Collectors.toList() ) );
		rabbitMQService.start();
		// UnirestService
		new UnirestService();
		// UpdateTrainingParameters
		final UpdateTrainingParametersService updateTrainingParametersService = new UpdateTrainingParametersService( pluginAppModel );
		( ( ShowPreferencesAction ) showPreferencesAction ).addSettingsListener( updateTrainingParametersService );
		// LoggerService
		( ( ShowPreferencesAction ) showPreferencesAction ).addSettingsListener( loggerService );
		// HighlightListener
		final HighlightListenerService highlightListenerService = new HighlightListenerService( pluginAppModel );
		pluginAppModel.getHighlightModel().listeners().add( highlightListenerService );
		// GraphListener
		final GraphListenerService graphListenerService = new GraphListenerService( pluginAppModel );
		pluginAppModel.getModel().getGraph().addGraphListener( graphListenerService );
		// VertexPositionListenerService
		final VertexPositionListenerService vertexPositionListenerService = new VertexPositionListenerService( pluginAppModel );
		pluginAppModel.getModel().getGraph().addVertexPositionListener( vertexPositionListenerService );
		// UpdateListener
		pluginAppModel.getKeymap().updateListeners().add( 0, this );
		// Create tag sets if not exists
		new SetUpTagSetsService( pluginAppModel );
		// Reset ControlAxis
		ElephantActionStateManager.INSTANCE.setAxis( ControlAxis.X );
	}

	@Override
	public List< MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "Plugins",
						menu( "ELEPHANT",
								menu( "Detection",
										item( predictSpotsAction.name() ),
										item( updateDetectionLabelsAction.name() ),
										item( resetDetectionLabelsAction.name() ),
										item( liveTrainingAction.name() ),
										item( trainSelectedAction.name() ),
										item( trainAllAction.name() ),
										item( resetDetectionModelAction.name() ),
										item( downloadDetectionModelAction.name() ) ),
								menu( "Linking",
										item( nnLinkingAction.name() ),
										item( updateFlowLabelsAction.name() ),
										item( resetFlowLabelsAction.name() ),
										item( trainFlowAction.name() ),
										item( resetFlowModelAction.name() ),
										item( downloadFlowModelAction.name() ) ),
								menu( "Utils",
										item( mapSpotTagAction.name() ),
										item( mapLinkTagAction.name() ),
										item( removeAllAction.name() ),
										item( removeShortTracksAction.name() ),
										item( removeSpotsByTagAction.name() ),
										item( removeEdgesByTagAction.name() ),
										item( removeVisibleSpotsAction.name() ),
										item( removeSelfLinksAction.name() ),
										item( takeSnapshotAction.name() ),
										item( recordSnapshotMovieAction.name() ),
										item( importMastodonAction.name() ),
										item( exportCTCAction.name() ),
										item( changeDetectionTagSetColorsAction.name() ) ),
								menu( "Analysis",
										item( tagProgenitorAction.name() ),
										item( tagProliferatorAction.name() ),
										item( tagDividingCellsAction.name() ),
										item( countDivisionsEntireAction.name() ),
										item( countDivisionsTrackwiseAction.name() ) ),
								menu( "Window",
										item( showLogWindowAction.name() ),
										item( showControlPanelAction.name() ) ),
								item( abortProcessingAction.name() ),
								item( showPreferencesAction.name() ) ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		final Map< String, String > menuTexts = new HashMap<>();
		for ( final AbstractElephantAction pluginAction : pluginActions )
			menuTexts.put( pluginAction.name(), pluginAction.getMenuText() );
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		pluginActions.forEach( pluginAction -> actions.namedAction( pluginAction, pluginAction.getMenuKeys() ) );
	}

	/**
	 * UpdateListener
	 */
	@Override
	public void keymapChanged()
	{
		installGlobalActions( pluginAppModel.getPlugins().getPluginActions() );
	}

	class Mastodon extends ContextCommand
	{

		private ProjectModel projectModel;

		private MainWindow mainWindow;

		private String fileOpenDialog()
		{
			JFileChooser fileChooser = new JFileChooser( "Open Mastodon Project" );
			fileChooser.setFileFilter( new FileNameExtensionFilter( "Mastodon Project (*.mastodon)", "mastodon" ) );
			fileChooser.showOpenDialog( null );
			return fileChooser.getSelectedFile().getAbsolutePath();
		}

		@Override
		public void run()
		{
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );
			String projectPath = fileOpenDialog();
			try
			{
				projectModel = ProjectLoader.open( projectPath, getContext() );
			}
			catch ( IOException | SpimDataException e )
			{
				e.printStackTrace();
			}
			mainWindow = new MainWindow( projectModel );
			mainWindow.setVisible( true );
		}

		// FOR TESTING ONLY!
		public void openProject( final MamutProject project ) throws IOException, SpimDataException
		{
			ProjectCreator.createProjectFromBdvFile( project.getDatasetXmlFile(), pluginAppModel.getContext() );
		}

		// FOR TESTING ONLY!
		public void setExitOnClose()
		{
			mainWindow.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		}

	}

	/**
	 * Main method
	 */
	public static void main( final String[] args ) throws Exception
	{
		setSystemLookAndFeelAndLocale();

		final Elephant elephant = new Elephant();
		final Mastodon mastodon = elephant.new Mastodon();;
		try ( final Context context = new Context() )
		{
			context.inject( mastodon );
			mastodon.run();
			mastodon.setExitOnClose();
			try
			{
				final ResourceBundle rb = ResourceBundle.getBundle( "default" );
				final MamutProject project = new MamutProjectIO().load( rb.getString( "project" ) );
				mastodon.openProject( project );
			}
			catch ( final Exception e )
			{
				System.out.println( "Loading from resource failed. Start with empty project." );
			}
		}
	}

	private static final void setSystemLookAndFeelAndLocale()
	{
		Locale.setDefault( Locale.ROOT );
		try
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e )
		{
			e.printStackTrace();
		}
	}
}
