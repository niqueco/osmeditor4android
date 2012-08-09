package de.blau.android.presets;

import java.io.FileInputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import de.blau.android.util.Hash;

public class PresetIconManager {
	
	private final Context context;
	
	/** base path for downloaded icons */
	private final String basePath;
	
	private final static String ASSET_IMAGE_PREFIX = "images/";
	
	/** Asset manager for global preset icons (with local paths) */
	private final AssetManager assets;
	
	/**
	 * Creates a new PresetIconManager.
	 * @param basePath base path for images downloaded for this preset. may be null.
	 */
	public PresetIconManager(Context context, String basePath) {
		this.context = context;
		this.assets = context.getAssets();
		this.basePath = basePath;
	}
	
	/**
	 * Gets a drawable for a URL.<br>
	 * If the URL is a HTTP(S) URL and a base path is given, it will be checked for the downloaded drawable.<br>
	 * Otherwise, the URL will be considered a relative path, checked for ".." to avoid path traversal,
	 * and it will be attempted to load the corresponding image from the asset image directory.<br>
	 * @param url either a local preset url of the format "presets/xyz.png", or a http/https url
	 * @param size icon size in dp
	 * @return null if icon file not found or a drawable of [size]x[size] dp.
	 */
	public BitmapDrawable getDrawable(String url, int size) {
		if (url == null) return null;
		
		InputStream pngStream = null;
		try {
			if (basePath != null && (url.startsWith("http://") || url.startsWith("https://"))) {
				pngStream = new FileInputStream(basePath+"/"+hash(url)+".png");
			} else if (!url.contains("..")) {
				pngStream = assets.open(ASSET_IMAGE_PREFIX+url);
			} else {
				Log.e("PresetIconManager", "unknown icon URL type for " + url);
				return null;
			}
			
			BitmapDrawable drawable = new BitmapDrawable(context.getResources(), pngStream);
			drawable.getBitmap().setDensity(Bitmap.DENSITY_NONE);
			int pxsize = dpToPx(size);
			drawable.setBounds(0, 0, pxsize, pxsize);
			return drawable;
		} catch (Exception e) {
			Log.e("PresetIconManager", "Failed to load preset icon " + url, e);
			return null;
		} finally {
			try { if (pngStream != null) pngStream.close(); } catch (Exception e) {} // ignore IO exceptions
		}
	}
	
	/**
	 * Like {@link #getDrawable(String, int)}, but returns a transparent placeholder
	 * instead of null
	 */
	public Drawable getDrawableOrPlaceholder(String url, int size) {
		Drawable result = getDrawable(url, size);
		if (result != null) {
			return result;
		} else {
			Drawable placeholder = new ColorDrawable(android.R.color.transparent);
			int pxsize = dpToPx(size);
			placeholder.setBounds(0,0, pxsize, pxsize);
			return placeholder;
		}
	}

	/**
	 * Converts a size in dp to pixels
	 * @param dp size in display point
	 * @return size in pixels (for the current display metrics)
	 */
	private int dpToPx(int dp) {
		return Math.round(dp * context.getResources().getDisplayMetrics().density);
	}
	
	/**
	 * Creates a unique identifier for the given value
	 * @param value the value to hash
	 * @return a unique, file-name safe identifier
	 */
	public static String hash(String value) {
		return Hash.sha256(value).substring(0, 24);
	}
	
}
