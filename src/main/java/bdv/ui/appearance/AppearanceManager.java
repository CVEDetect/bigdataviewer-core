package bdv.ui.appearance;

import java.awt.Component;
import java.awt.Window;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import bdv.ui.UIUtils;
import bdv.util.Prefs;

/**
 * Manages the {@link Appearance} (save/load config file, propagate changes to
 * {@link Prefs} and LookAndFeel).
 */
public class AppearanceManager
{
	private static final String CONFIG_FILE_NAME = "appearance.yaml";

	private final String configFile;

	private final List< WeakReference< Component > > components = new CopyOnWriteArrayList<>();

	/**
	 * The managed Appearance. This will be updated with changes from the
	 * Preferences (on "Apply" or "Ok").
	 */
	private final Appearance appearance;

	public AppearanceManager()
	{
		this( null );
	}

	public AppearanceManager( final String configDir )
	{
		configFile = configDir == null ? null : configDir + "/" + CONFIG_FILE_NAME;
		appearance = new Appearance();
		fromPrefs();
		load();
		toPrefs();
	}

	public Appearance appearance()
	{
		return appearance;
	}

	/**
	 * Add components here that should be notified via {@cpde
	 * SwingUtilities.updateComponentTreeUI()} when the Look-And-Feel is
	 * changed.
	 * <p>
	 * Note that all {@link Window#getWindows() Windows} will be notified anyway,
	 * so most things do not need to be registered here ({@code JFileChooser} is
	 * a notable exception.)
	 */
	public void addLafComponent( final Component component )
	{
		components.add( new WeakReference<>( component ) );
	}

	public void updateLookAndFeel()
	{
		final UIManager.LookAndFeelInfo laf = appearance.lookAndFeel();
		if ( laf == Appearance.DONT_MODIFY_LOOK_AND_FEEL )
			return;
		if ( UIManager.getLookAndFeel().getName().equals( laf.getName() ) )
			return;
		try
		{
			UIUtils.reset();

			UIManager.setLookAndFeel( laf.getClassName() );

			for ( final Window window : Window.getWindows() )
				SwingUtilities.updateComponentTreeUI( window );

			final List< WeakReference< Component > > remove = new ArrayList<>();
			for ( final WeakReference< Component > ref : components )
			{
				final Component component = ref.get();
				if ( component == null )
					remove.add( ref );
				else
					SwingUtilities.updateComponentTreeUI( component );
			}
			components.removeAll( remove );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

	void load()
	{
		load( configFile );
	}

	void load( final String filename )
	{
		try
		{
			final Appearance a = AppearanceIO.load( filename );
			appearance.set( a );
		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "Appearance settings file " + filename + " not found. Using defaults." );
		}
		catch ( final Exception e )
		{
			System.out.println( "Error while reading appearance settings file " + filename + ". Using defaults." );
		}
	}

	void save()
	{
		save( configFile );
	}

	void save( final String filename )
	{
		try
		{
			AppearanceIO.save( appearance, filename );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
			System.out.println( "Error while reading appearance settings file " + filename + ". Using defaults." );
		}
	}

	/**
	 * @deprecated Prefs will be replaced eventually by directly using {@code Appearance} in BDV
	 */
	@Deprecated
	public void toPrefs()
	{
		Prefs.showScaleBar( appearance.showScaleBar() );
		Prefs.showScaleBarInMovie( appearance.showScaleBarInMovie() );
		Prefs.showMultibox( appearance.showMultibox() );
		Prefs.showTextOverlay( appearance.showTextOverlay() );
		Prefs.sourceNameOverlayPosition( appearance.sourceNameOverlayPosition() );
		Prefs.scaleBarColor( appearance.scaleBarColor() );
		Prefs.scaleBarBgColor( appearance.scaleBarBgColor() );
	}

	/**
	 * @deprecated Prefs will be replaced eventually by directly using {@code Appearance} in BDV
	 */
	@Deprecated
	public void fromPrefs()
	{
		appearance.setShowScaleBar( Prefs.showScaleBar() );
		appearance.setShowScaleBarInMovie( Prefs.showScaleBarInMovie() );
		appearance.setShowMultibox( Prefs.showMultibox() );
		appearance.setShowTextOverlay( Prefs.showTextOverlay() );
		appearance.setSourceNameOverlayPosition( Prefs.sourceNameOverlayPosition() );
		appearance.setScaleBarColor( Prefs.scaleBarColor() );
		appearance.setScaleBarBgColor( Prefs.scaleBarBgColor() );
	}
}
