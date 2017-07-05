/**
 *
 */
package com.jug.mmpreprocess;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jug.mmpreprocess.util.FloatTypeImgLoader;


/**
 * @author jug
 */
public class MMDataSource {

	private final String[] extensions = { "tif", "tiff" };

	private final List< MMDataFrame > dataFrames = new ArrayList< MMDataFrame >();

	private final int numChannels;

	/**
	 * @param inputFolder
	 */
	public MMDataSource(
			final File inputFolder,
			final int numChannels,
			final int minChannelIdx,
			final int minTime,
			int maxTime ) {
		sanityChecks( inputFolder );

		this.numChannels = numChannels;

		if ( maxTime < 0 ) {
			maxTime = Integer.MAX_VALUE;
		}

		final File[] fileArray =
				inputFolder.listFiles( /*new ExtensionFileFilter( extensions, ".tif and .tiff" )*/ MMUtils.tifFilter );
		final List< String > listOfImageFilesnames = new ArrayList<>( fileArray.length );
		for ( final File file : fileArray ) {
			if ( !file.isDirectory() ) {
				if ( isInDataRange( file.getAbsolutePath(), minTime, maxTime ) ) {
					listOfImageFilesnames.add( file.getAbsolutePath() );
				}
			}
		}

		if ( fileArray.length == 0 ) {
			System.err.println(
					"Valid files found for processing: " + listOfImageFilesnames
							.size() + " of " + fileArray.length + " (filtered by " + minTime + " < t < " + maxTime + ")" );
			System.exit( 1 );
		} else {
			System.out.println(
					"Valid files found for processing: " + listOfImageFilesnames
							.size() + " of " + fileArray.length + " (filtered by " + minTime + " < t < " + maxTime + ")" );
		}

		Collections.sort( listOfImageFilesnames );
		int i = 0;
		List< String > srcFilenames = new ArrayList<>( numChannels );
		for ( final String filename : listOfImageFilesnames ) {
//			System.out.println( filename );

			// collect
			if ( i % numChannels == 0 ) {
				srcFilenames.add( filename );
			} else {
				srcFilenames.add( filename );
			}
			// add + restart collecting
			if ( ( i + 1 ) % numChannels == 0 ) {
				dataFrames.add( new MMDataFrame( srcFilenames, numChannels, minChannelIdx, inputFolder.getName() ) );
				srcFilenames = new ArrayList<>( numChannels );
			}

			i++;
		}
	}

	/**
	 * @return
	 */
	private boolean isInDataRange( final String fn, final int minT, final int maxT ) {
		//final int start = fn.indexOf( "_t" ) + 2;
		//final String strT = fn.substring( start, start + 4 );
		try {
			final int t = FloatTypeImgLoader.getTimeFromFilename(fn); //Integer.parseInt( strT );
			if ( t >= minT && t <= maxT ) { return true; }
		} catch ( final NumberFormatException e ) {
			throw new IllegalArgumentException( String.format(
					"ERROR\tFile list corrupt. Time could not be extracted for file %s.", fn ) );
		}
		return false;
	}

	/**
	 * @param inputFolder
	 */
	public void sanityChecks( final File inputFolder ) {
		if ( inputFolder == null ) {
			throw new IllegalArgumentException( "ERROR\tGiven input folder is null." );
		}
		if ( !inputFolder.canRead() ) {
			throw new IllegalArgumentException( String.format(
				"ERROR\tGiven input folder can not be read - check permissions (%s).",
				inputFolder.getAbsolutePath() ) );
		}
		if ( !inputFolder.isDirectory() ) {
			throw new IllegalArgumentException( String.format(
				"ERROR\tGiven input folder is invalid (%s).",
				inputFolder.getAbsolutePath() ) );
		}
	}

	public MMDataFrame getFrame( final int i ) {
		return dataFrames.get( i );
	}

	/**
	 * @return
	 */
	public int size() {
		return dataFrames.size();
	}

}
