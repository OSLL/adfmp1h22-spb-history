package ru.info.spb_info//package com.otaliastudios.cameraview
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.annotation.TargetApi
//import android.app.Activity
//import android.content.Context
//import android.content.ContextWrapper
//import android.content.pm.PackageInfo
//import android.content.pm.PackageManager
//import android.content.res.TypedArray
//import android.graphics.PointF
//import android.graphics.Rect
//import android.graphics.RectF
//import android.location.Location
//import android.media.MediaActionSound
//import android.os.Build
//import android.os.Handler
//import android.os.Looper
//import android.util.AttributeSet
//import android.view.MotionEvent
//import android.view.View
//import android.view.View.MeasureSpec
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import androidx.annotation.ColorInt
//import androidx.annotation.VisibleForTesting
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleObserver
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.OnLifecycleEvent
//import com.otaliastudios.cameraview.controls.Audio
//import com.otaliastudios.cameraview.controls.AudioCodec
//import com.otaliastudios.cameraview.controls.Control
//import com.otaliastudios.cameraview.controls.ControlParser
//import com.otaliastudios.cameraview.controls.Engine
//import com.otaliastudios.cameraview.controls.Facing
//import com.otaliastudios.cameraview.controls.Flash
//import com.otaliastudios.cameraview.controls.Grid
//import com.otaliastudios.cameraview.controls.Hdr
//import com.otaliastudios.cameraview.controls.Mode
//import com.otaliastudios.cameraview.controls.PictureFormat
//import com.otaliastudios.cameraview.controls.Preview
//import com.otaliastudios.cameraview.controls.VideoCodec
//import com.otaliastudios.cameraview.controls.WhiteBalance
//import com.otaliastudios.cameraview.engine.Camera1Engine
//import com.otaliastudios.cameraview.engine.Camera2Engine
//import com.otaliastudios.cameraview.engine.CameraEngine
//import com.otaliastudios.cameraview.engine.offset.Reference
//import com.otaliastudios.cameraview.engine.orchestrator.CameraState
//import com.otaliastudios.cameraview.filter.Filter
//import com.otaliastudios.cameraview.filter.FilterParser
//import com.otaliastudios.cameraview.filter.Filters
//import com.otaliastudios.cameraview.filter.NoFilter
//import com.otaliastudios.cameraview.filter.OneParameterFilter
//import com.otaliastudios.cameraview.filter.TwoParameterFilter
//import com.otaliastudios.cameraview.frame.Frame
//import com.otaliastudios.cameraview.frame.FrameProcessor
//import com.otaliastudios.cameraview.gesture.Gesture
//import com.otaliastudios.cameraview.gesture.GestureAction
//import com.otaliastudios.cameraview.gesture.GestureFinder
//import com.otaliastudios.cameraview.gesture.GestureParser
//import com.otaliastudios.cameraview.gesture.PinchGestureFinder
//import com.otaliastudios.cameraview.gesture.ScrollGestureFinder
//import com.otaliastudios.cameraview.gesture.TapGestureFinder
//import com.otaliastudios.cameraview.internal.GridLinesLayout
//import com.otaliastudios.cameraview.internal.CropHelper
//import com.otaliastudios.cameraview.internal.OrientationHelper
//import com.otaliastudios.cameraview.markers.AutoFocusMarker
//import com.otaliastudios.cameraview.markers.AutoFocusTrigger
//import com.otaliastudios.cameraview.markers.MarkerLayout
//import com.otaliastudios.cameraview.markers.MarkerParser
//import com.otaliastudios.cameraview.metering.MeteringRegions
//import com.otaliastudios.cameraview.overlay.OverlayLayout
//import com.otaliastudios.cameraview.preview.CameraPreview
//import com.otaliastudios.cameraview.preview.FilterCameraPreview
//import com.otaliastudios.cameraview.preview.GlCameraPreview
//import com.otaliastudios.cameraview.preview.SurfaceCameraPreview
//import com.otaliastudios.cameraview.preview.TextureCameraPreview
//import com.otaliastudios.cameraview.size.AspectRatio
//import com.otaliastudios.cameraview.size.Size
//import com.otaliastudios.cameraview.size.SizeSelector
//import com.otaliastudios.cameraview.size.SizeSelectorParser
//import com.otaliastudios.cameraview.size.SizeSelectors
//import java.io.File
//import java.io.FileDescriptor
//import java.lang.Exception
//import java.lang.IllegalArgumentException
//import java.lang.IllegalStateException
//import java.lang.RuntimeException
//import java.util.ArrayList
//import java.util.HashMap
//import java.util.concurrent.*
//import java.util.concurrent.atomic.AtomicInteger
//
///**
// * Entry point for the whole library.
// * Please read documentation for usage and full set of features.
// */
//class CameraView : FrameLayout, LifecycleObserver {
//    // Self managed parameters
//    private var mPlaySounds: Boolean = false
//    private var mUseDeviceOrientation: Boolean = false
//    private var mRequestPermissions: Boolean = false
//    private val mGestureMap: HashMap<Gesture, GestureAction> = HashMap<Gesture, GestureAction>(4)
//    private var mPreview: Preview? = null
//    private var mEngine: Engine? = null
//    private var mPendingFilter: Filter? = null
//    private var mFrameProcessingExecutors: Int = 0
//    private var mActiveGestures: Int = 0
//
//    // Components
//    private var mUiHandler: Handler? = null
//    private var mFrameProcessingExecutor: Executor? = null
//
//    @VisibleForTesting
//    var mCameraCallbacks: CameraCallbacks? = null
//    private var mCameraPreview: CameraPreview? = null
//    private var mOrientationHelper: OrientationHelper? = null
//    private var mCameraEngine: CameraEngine? = null
//    private var mLastPreviewStreamSize: Size? = null
//    private var mSound: MediaActionSound? = null
//    private var mAutoFocusMarker: AutoFocusMarker? = null
//
//    @VisibleForTesting
//    var mListeners: MutableList<CameraListener> = CopyOnWriteArrayList<CameraListener>()
//
//    @VisibleForTesting
//    var mFrameProcessors: MutableList<FrameProcessor> = CopyOnWriteArrayList<FrameProcessor>()
//    private var mLifecycle: Lifecycle? = null
//
//    // Gestures
//    @VisibleForTesting
//    var mPinchGestureFinder: PinchGestureFinder? = null
//
//    @VisibleForTesting
//    var mTapGestureFinder: TapGestureFinder? = null
//
//    @VisibleForTesting
//    var mScrollGestureFinder: ScrollGestureFinder? = null
//
//    // Views
//    @VisibleForTesting
//    var mGridLinesLayout: GridLinesLayout? = null
//
//    @VisibleForTesting
//    var mMarkerLayout: MarkerLayout? = null
//    private var mKeepScreenOn: Boolean = false
//    private var mExperimental: Boolean = false
//    private var mInEditor: Boolean = false
//
//    // Overlays
//    @VisibleForTesting
//    var mOverlayLayout: OverlayLayout? = null
//
//    constructor(context: Context) : super(context, null) {
//        initialize(context, null)
//    }
//
//    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
//        initialize(context, attrs)
//    }
//
//    //region Init
//    private fun initialize(context: Context, attrs: AttributeSet?) {
//        mInEditor = isInEditMode
//        if (mInEditor) return
//        setWillNotDraw(false)
//        val a: TypedArray = context.theme.obtainStyledAttributes(
//            attrs, R.styleable.CameraView,
//            0, 0
//        )
//        val controls: ControlParser = ControlParser(context, a)
//
//        // Self managed
//        val playSounds: Boolean = a.getBoolean(
//            R.styleable.CameraView_cameraPlaySounds,
//            DEFAULT_PLAY_SOUNDS
//        )
//        val useDeviceOrientation: Boolean = a.getBoolean(
//            R.styleable.CameraView_cameraUseDeviceOrientation, DEFAULT_USE_DEVICE_ORIENTATION
//        )
//        mExperimental = a.getBoolean(R.styleable.CameraView_cameraExperimental, false)
//        mRequestPermissions = a.getBoolean(
//            R.styleable.CameraView_cameraRequestPermissions,
//            DEFAULT_REQUEST_PERMISSIONS
//        )
//        mPreview = controls.getPreview()
//        mEngine = controls.getEngine()
//
//        // Camera engine params
//        val gridColor: Int = a.getColor(
//            R.styleable.CameraView_cameraGridColor,
//            GridLinesLayout.DEFAULT_COLOR
//        )
//        val videoMaxSize: Long = a.getFloat(R.styleable.CameraView_cameraVideoMaxSize, 0f).toLong()
//        val videoMaxDuration: Int = a.getInteger(
//            R.styleable.CameraView_cameraVideoMaxDuration,
//            0
//        )
//        val videoBitRate: Int = a.getInteger(R.styleable.CameraView_cameraVideoBitRate, 0)
//        val audioBitRate: Int = a.getInteger(R.styleable.CameraView_cameraAudioBitRate, 0)
//        val videoFrameRate: Float = a.getFloat(R.styleable.CameraView_cameraPreviewFrameRate, 0f)
//        val videoFrameRateExact: Boolean =
//            a.getBoolean(R.styleable.CameraView_cameraPreviewFrameRateExact, false)
//        val autoFocusResetDelay: Long = a.getInteger(
//            R.styleable.CameraView_cameraAutoFocusResetDelay,
//            DEFAULT_AUTOFOCUS_RESET_DELAY_MILLIS.toInt()
//        ).toLong()
//        val pictureMetering: Boolean = a.getBoolean(
//            R.styleable.CameraView_cameraPictureMetering,
//            DEFAULT_PICTURE_METERING
//        )
//        val pictureSnapshotMetering: Boolean = a.getBoolean(
//            R.styleable.CameraView_cameraPictureSnapshotMetering,
//            DEFAULT_PICTURE_SNAPSHOT_METERING
//        )
//        val snapshotMaxWidth: Int = a.getInteger(R.styleable.CameraView_cameraSnapshotMaxWidth, 0)
//        val snapshotMaxHeight: Int = a.getInteger(R.styleable.CameraView_cameraSnapshotMaxHeight, 0)
//        val frameMaxWidth: Int =
//            a.getInteger(R.styleable.CameraView_cameraFrameProcessingMaxWidth, 0)
//        val frameMaxHeight: Int =
//            a.getInteger(R.styleable.CameraView_cameraFrameProcessingMaxHeight, 0)
//        val frameFormat: Int = a.getInteger(R.styleable.CameraView_cameraFrameProcessingFormat, 0)
//        val framePoolSize: Int = a.getInteger(
//            R.styleable.CameraView_cameraFrameProcessingPoolSize,
//            DEFAULT_FRAME_PROCESSING_POOL_SIZE
//        )
//        val frameExecutors: Int = a.getInteger(
//            R.styleable.CameraView_cameraFrameProcessingExecutors,
//            DEFAULT_FRAME_PROCESSING_EXECUTORS
//        )
//        val drawHardwareOverlays: Boolean =
//            a.getBoolean(R.styleable.CameraView_cameraDrawHardwareOverlays, false)
//
//        // Size selectors and gestures
//        val sizeSelectors: SizeSelectorParser = SizeSelectorParser(a)
//        val gestures: GestureParser = GestureParser(a)
//        val markers: MarkerParser = MarkerParser(a)
//        val filters: FilterParser = FilterParser(a)
//        a.recycle()
//
//        // Components
//        mCameraCallbacks = CameraCallbacks()
//        mUiHandler = Handler(Looper.getMainLooper())
//
//        // Gestures
//        mPinchGestureFinder = PinchGestureFinder(mCameraCallbacks)
//        mTapGestureFinder = TapGestureFinder(mCameraCallbacks)
//        mScrollGestureFinder = ScrollGestureFinder(mCameraCallbacks)
//
//        // Views
//        mGridLinesLayout = GridLinesLayout(context)
//        mOverlayLayout = OverlayLayout(context)
//        mMarkerLayout = MarkerLayout(context)
//        addView(mGridLinesLayout)
//        addView(mMarkerLayout)
//        addView(mOverlayLayout)
//
//        // Create the engine
//        doInstantiateEngine()
//
//        // Apply self managed
//        setPlaySounds(playSounds)
//        setUseDeviceOrientation(useDeviceOrientation)
//        setGrid(controls.getGrid())
//        setGridColor(gridColor)
//        setDrawHardwareOverlays(drawHardwareOverlays)
//
//        // Apply camera engine params
//        // Adding new ones? See setEngine().
//        setFacing(controls.getFacing())
//        setFlash(controls.getFlash())
//        setMode(controls.getMode())
//        setWhiteBalance(controls.getWhiteBalance())
//        setHdr(controls.getHdr())
//        setAudio(controls.getAudio())
//        setAudioBitRate(audioBitRate)
//        setAudioCodec(controls.getAudioCodec())
//        setPictureSize(sizeSelectors.getPictureSizeSelector())
//        setPictureMetering(pictureMetering)
//        setPictureSnapshotMetering(pictureSnapshotMetering)
//        setPictureFormat(controls.getPictureFormat())
//        setVideoSize(sizeSelectors.getVideoSizeSelector())
//        setVideoCodec(controls.getVideoCodec())
//        setVideoMaxSize(videoMaxSize)
//        setVideoMaxDuration(videoMaxDuration)
//        setVideoBitRate(videoBitRate)
//        setAutoFocusResetDelay(autoFocusResetDelay)
//        setPreviewFrameRateExact(videoFrameRateExact)
//        setPreviewFrameRate(videoFrameRate)
//        setSnapshotMaxWidth(snapshotMaxWidth)
//        setSnapshotMaxHeight(snapshotMaxHeight)
//        setFrameProcessingMaxWidth(frameMaxWidth)
//        setFrameProcessingMaxHeight(frameMaxHeight)
//        setFrameProcessingFormat(frameFormat)
//        setFrameProcessingPoolSize(framePoolSize)
//        setFrameProcessingExecutors(frameExecutors)
//
//        // Apply gestures
//        mapGesture(Gesture.TAP, gestures.getTapAction())
//        mapGesture(Gesture.LONG_TAP, gestures.getLongTapAction())
//        mapGesture(Gesture.PINCH, gestures.getPinchAction())
//        mapGesture(Gesture.SCROLL_HORIZONTAL, gestures.getHorizontalScrollAction())
//        mapGesture(Gesture.SCROLL_VERTICAL, gestures.getVerticalScrollAction())
//
//        // Apply markers
//        setAutoFocusMarker(markers.getAutoFocusMarker())
//
//        // Apply filters
//        setFilter(filters.getFilter())
//
//        // Create the orientation helper
//        mOrientationHelper = OrientationHelper(context, mCameraCallbacks)
//    }
//
//    /**
//     * Engine is instantiated on creation and anytime
//     * [.setEngine] is called.
//     */
//    private fun doInstantiateEngine() {
//        LOG.w("doInstantiateEngine:", "instantiating. engine:", mEngine)
//        mCameraEngine = instantiateCameraEngine(mEngine, mCameraCallbacks)
//        LOG.w(
//            "doInstantiateEngine:", "instantiated. engine:",
//            mCameraEngine.getClass().getSimpleName()
//        )
//        mCameraEngine.setOverlay(mOverlayLayout)
//    }
//
//    /**
//     * Preview is instantiated [.onAttachedToWindow], because
//     * we want to know if we're hardware accelerated or not.
//     * However, in tests, we might want to create the preview right after constructor.
//     */
//    @VisibleForTesting
//    fun doInstantiatePreview() {
//        LOG.w("doInstantiateEngine:", "instantiating. preview:", mPreview)
//        mCameraPreview = instantiatePreview(mPreview, context, this)
//        LOG.w(
//            "doInstantiateEngine:", "instantiated. preview:",
//            mCameraPreview.getClass().getSimpleName()
//        )
//        mCameraEngine.setPreview(mCameraPreview)
//        if (mPendingFilter != null) {
//            setFilter(mPendingFilter)
//            mPendingFilter = null
//        }
//    }
//
//    /**
//     * Instantiates the camera engine.
//     *
//     * @param engine the engine preference
//     * @param callback the engine callback
//     * @return the engine
//     */
//    protected fun instantiateCameraEngine(
//        engine: Engine,
//        callback: CameraEngine.Callback
//    ): CameraEngine {
//        if ((mExperimental
//                    && (engine === Engine.CAMERA2
//                    ) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP))
//        ) {
//            return Camera2Engine(callback)
//        } else {
//            mEngine = Engine.CAMERA1
//            return Camera1Engine(callback)
//        }
//    }
//
//    /**
//     * Instantiates the camera preview.
//     *
//     * @param preview current preview value
//     * @param context a context
//     * @param container the container
//     * @return the preview
//     */
//    protected fun instantiatePreview(
//        preview: Preview,
//        context: Context,
//        container: ViewGroup
//    ): CameraPreview {
//        when (preview) {
//            SURFACE -> return SurfaceCameraPreview(context, container)
//            TEXTURE -> {
//                run {
//                    if (isHardwareAccelerated()) {
//                        // TextureView is not supported without hardware acceleration.
//                        return TextureCameraPreview(context, container)
//                    }
//                }
//                run {
//                    mPreview = Preview.GL_SURFACE
//                    return GlCameraPreview(context, container)
//                }
//            }
//            GL_SURFACE -> {
//                mPreview = Preview.GL_SURFACE
//                return GlCameraPreview(context, container)
//            }
//            else -> {
//                mPreview = Preview.GL_SURFACE
//                return GlCameraPreview(context, container)
//            }
//        }
//    }
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        if (mInEditor) return
//        if (mCameraPreview == null) {
//            // isHardwareAccelerated will return the real value only after we are
//            // attached. That's why we instantiate the preview here.
//            doInstantiatePreview()
//        }
//    }
//
//    override fun onDetachedFromWindow() {
//        mLastPreviewStreamSize = null
//        super.onDetachedFromWindow()
//    }
//
//    //endregion
//    //region Measuring behavior
//    private fun ms(mode: Int): String? {
//        when (mode) {
//            MeasureSpec.AT_MOST -> return "AT_MOST"
//            MeasureSpec.EXACTLY -> return "EXACTLY"
//            MeasureSpec.UNSPECIFIED -> return "UNSPECIFIED"
//        }
//        return null
//    }
//
//    /**
//     * Measuring is basically controlled by layout params width and height.
//     * The basic semantics are:
//     *
//     * - MATCH_PARENT: CameraView should completely fill this dimension, even if this might mean
//     * not respecting the preview aspect ratio.
//     * - WRAP_CONTENT: CameraView should try to adapt this dimension to respect the preview
//     * aspect ratio.
//     *
//     * When both dimensions are MATCH_PARENT, CameraView will fill its
//     * parent no matter the preview. Thanks to what happens in [CameraPreview], this acts like
//     * a CENTER CROP scale type.
//     *
//     * When both dimensions are WRAP_CONTENT, CameraView will take the biggest dimensions that
//     * fit the preview aspect ratio. This acts like a CENTER INSIDE scale type.
//     */
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        if (mInEditor) {
//            val width: Int = MeasureSpec.getSize(widthMeasureSpec)
//            val height: Int = MeasureSpec.getSize(heightMeasureSpec)
//            super.onMeasure(
//                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
//            )
//            return
//        }
//        mLastPreviewStreamSize = mCameraEngine.getPreviewStreamSize(Reference.VIEW)
//        if (mLastPreviewStreamSize == null) {
//            LOG.w("onMeasure:", "surface is not ready. Calling default behavior.")
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//            return
//        }
//
//        // Let's which dimensions need to be adapted.
//        var widthMode: Int = MeasureSpec.getMode(widthMeasureSpec)
//        var heightMode: Int = MeasureSpec.getMode(heightMeasureSpec)
//        val widthValue: Int = MeasureSpec.getSize(widthMeasureSpec)
//        val heightValue: Int = MeasureSpec.getSize(heightMeasureSpec)
//        val previewWidth: Float = mLastPreviewStreamSize.getWidth()
//        val previewHeight: Float = mLastPreviewStreamSize.getHeight()
//
//        // Pre-process specs
//        val lp: ViewGroup.LayoutParams = layoutParams
//        if (!mCameraPreview.supportsCropping()) {
//            // We can't allow EXACTLY constraints in this case.
//            if (widthMode == MeasureSpec.EXACTLY) widthMode = MeasureSpec.AT_MOST
//            if (heightMode == MeasureSpec.EXACTLY) heightMode = MeasureSpec.AT_MOST
//        } else {
//            // If MATCH_PARENT is interpreted as AT_MOST, transform to EXACTLY
//            // to be consistent with our semantics (and our docs).
//            if (widthMode == MeasureSpec.AT_MOST && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) widthMode =
//                MeasureSpec.EXACTLY
//            if (heightMode == MeasureSpec.AT_MOST && lp.height == ViewGroup.LayoutParams.MATCH_PARENT) heightMode =
//                MeasureSpec.EXACTLY
//        }
//        LOG.i(
//            "onMeasure:", ("requested dimensions are ("
//                    + widthValue + "[" + ms(widthMode) + "]x"
//                    + heightValue + "[" + ms(heightMode) + "])")
//        )
//        LOG.i(
//            "onMeasure:", "previewSize is", ("("
//                    + previewWidth + "x" + previewHeight + ")")
//        )
//
//        // (1) If we have fixed dimensions (either 300dp or MATCH_PARENT), there's nothing we
//        // should do, other than respect it. The preview will eventually be cropped at the sides
//        // (by Preview scaling) except the case in which these fixed dimensions manage to fit
//        // exactly the preview aspect ratio.
//        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
//            LOG.i(
//                "onMeasure:", "both are MATCH_PARENT or fixed value. We adapt.",
//                "This means CROP_CENTER.", "(" + widthValue + "x" + heightValue + ")"
//            )
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//            return
//        }
//
//        // (2) If both dimensions are free, with no limits, then our size will be exactly the
//        // preview size. This can happen rarely, for example in 2d scrollable containers.
//        if (widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED) {
//            LOG.i(
//                "onMeasure:", "both are completely free.",
//                "We respect that and extend to the whole preview size.",
//                "(" + previewWidth + "x" + previewHeight + ")"
//            )
//            super.onMeasure(
//                MeasureSpec.makeMeasureSpec(previewWidth.toInt(), MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(previewHeight.toInt(), MeasureSpec.EXACTLY)
//            )
//            return
//        }
//
//        // It's sure now that at least one dimension can be determined (either because EXACTLY
//        // or AT_MOST). This starts to seem a pleasant situation.
//
//        // (3) If one of the dimension is completely free (e.g. in a scrollable container),
//        // take the other and fit the ratio.
//        // One of the two might be AT_MOST, but we use the value anyway.
//        val ratio: Float = previewHeight / previewWidth
//        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
//            val freeWidth: Boolean = widthMode == MeasureSpec.UNSPECIFIED
//            val height: Int
//            val width: Int
//            if (freeWidth) {
//                height = heightValue
//                width = Math.round(height / ratio)
//            } else {
//                width = widthValue
//                height = Math.round(width * ratio)
//            }
//            LOG.i(
//                "onMeasure:", "one dimension was free, we adapted it to fit the ratio.",
//                "(" + width + "x" + height + ")"
//            )
//            super.onMeasure(
//                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
//            )
//            return
//        }
//
//        // (4) At this point both dimensions are either AT_MOST-AT_MOST, EXACTLY-AT_MOST or
//        // AT_MOST-EXACTLY. Let's manage this sanely. If only one is EXACTLY, we can TRY to fit
//        // the aspect ratio, but it is not guaranteed to succeed. It depends on the AT_MOST
//        // value of the other dimensions.
//        if (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY) {
//            val freeWidth: Boolean = widthMode == MeasureSpec.AT_MOST
//            val height: Int
//            val width: Int
//            if (freeWidth) {
//                height = heightValue
//                width = Math.min(Math.round(height / ratio), widthValue)
//            } else {
//                width = widthValue
//                height = Math.min(Math.round(width * ratio), heightValue)
//            }
//            LOG.i(
//                "onMeasure:", "one dimension was EXACTLY, another AT_MOST.",
//                "We have TRIED to fit the aspect ratio, but it's not guaranteed.",
//                "(" + width + "x" + height + ")"
//            )
//            super.onMeasure(
//                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
//            )
//            return
//        }
//
//        // (5) Last case, AT_MOST and AT_MOST. Here we can SURELY fit the aspect ratio by
//        // filling one dimension and adapting the other.
//        val height: Int
//        val width: Int
//        val atMostRatio: Float = heightValue.toFloat() / widthValue.toFloat()
//        if (atMostRatio >= ratio) {
//            // We must reduce height.
//            width = widthValue
//            height = Math.round(width * ratio)
//        } else {
//            height = heightValue
//            width = Math.round(height / ratio)
//        }
//        LOG.i(
//            "onMeasure:", "both dimension were AT_MOST.",
//            "We fit the preview aspect ratio.",
//            "(" + width + "x" + height + ")"
//        )
//        super.onMeasure(
//            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
//            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
//        )
//    }
//    //endregion
//    //region Gesture APIs
//    /**
//     * Maps a [Gesture] to a certain gesture action.
//     * For example, you can assign zoom control to the pinch gesture by just calling:
//     * `
//     * cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
//    ` *
//     *
//     * Not all actions can be assigned to a certain gesture. For example, zoom control can't be
//     * assigned to the Gesture.TAP gesture. Look at [Gesture] to know more.
//     * This method returns false if they are not assignable.
//     *
//     * @param gesture which gesture to map
//     * @param action which action should be assigned
//     * @return true if this action could be assigned to this gesture
//     */
//    fun mapGesture(gesture: Gesture, action: GestureAction): Boolean {
//        val none: GestureAction = GestureAction.NONE
//        if (gesture.isAssignableTo(action)) {
//            mGestureMap[gesture] = action
//            when (gesture) {
//                PINCH -> mPinchGestureFinder.setActive(mGestureMap.get(Gesture.PINCH) !== none)
//                TAP, LONG_TAP -> mTapGestureFinder.setActive(
//                    mGestureMap.get(Gesture.TAP) !== none ||  // mGestureMap.get(Gesture.DOUBLE_TAP) != none ||
//                            mGestureMap.get(Gesture.LONG_TAP) !== none
//                )
//                SCROLL_HORIZONTAL, SCROLL_VERTICAL -> mScrollGestureFinder.setActive(
//                    mGestureMap.get(Gesture.SCROLL_HORIZONTAL) !== none ||
//                            mGestureMap.get(Gesture.SCROLL_VERTICAL) !== none
//                )
//            }
//            mActiveGestures = 0
//            for (act: GestureAction in mGestureMap.values) {
//                mActiveGestures += if (act === GestureAction.NONE) 0 else 1
//            }
//            return true
//        }
//        mapGesture(gesture, none)
//        return false
//    }
//
//    /**
//     * Clears any action mapped to the given gesture.
//     * @param gesture which gesture to clear
//     */
//    fun clearGesture(gesture: Gesture) {
//        mapGesture(gesture, GestureAction.NONE)
//    }
//
//    /**
//     * Returns the action currently mapped to the given gesture.
//     *
//     * @param gesture which gesture to inspect
//     * @return mapped action
//     */
//    fun getGestureAction(gesture: Gesture): GestureAction {
//        return mGestureMap.get(gesture)
//    }
//
//    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
//        // Steal our own events if gestures are enabled
//        return mActiveGestures > 0
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (!isOpened) return true
//
//        // Pass to our own GestureLayouts
//        val options: CameraOptions? = mCameraEngine.getCameraOptions() // Non null
//        if (options == null) throw IllegalStateException("Options should not be null here.")
//        if (mPinchGestureFinder.onTouchEvent(event)) {
//            LOG.i("onTouchEvent", "pinch!")
//            onGesture(mPinchGestureFinder, options)
//        } else if (mScrollGestureFinder.onTouchEvent(event)) {
//            LOG.i("onTouchEvent", "scroll!")
//            onGesture(mScrollGestureFinder, options)
//        } else if (mTapGestureFinder.onTouchEvent(event)) {
//            LOG.i("onTouchEvent", "tap!")
//            onGesture(mTapGestureFinder, options)
//        }
//        return true
//    }
//
//    // Some gesture layout detected a gesture. It's not known at this moment:
//    // (1) if it was mapped to some action (we check here)
//    // (2) if it's supported by the camera (CameraEngine checks)
//    private fun onGesture(source: GestureFinder, options: CameraOptions) {
//        val gesture: Gesture = source.getGesture()
//        val action: GestureAction? = mGestureMap.get(gesture)
//        val points: Array<PointF> = source.getPoints()
//        val oldValue: Float
//        val newValue: Float
//        when (action) {
//            TAKE_PICTURE_SNAPSHOT -> takePictureSnapshot()
//            TAKE_PICTURE -> takePicture()
//            AUTO_FOCUS -> {
//                val size: Size = Size(width, height)
//                val regions: MeteringRegions = MeteringRegions.fromPoint(size, points.get(0))
//                mCameraEngine.startAutoFocus(gesture, regions, points.get(0))
//            }
//            ZOOM -> {
//                oldValue = mCameraEngine.getZoomValue()
//                newValue = source.computeValue(oldValue, 0, 1)
//                if (newValue != oldValue) {
//                    mCameraEngine.setZoom(newValue, points, true)
//                }
//            }
//            EXPOSURE_CORRECTION -> {
//                oldValue = mCameraEngine.getExposureCorrectionValue()
//                val minValue: Float = options.getExposureCorrectionMinValue()
//                val maxValue: Float = options.getExposureCorrectionMaxValue()
//                newValue = source.computeValue(oldValue, minValue, maxValue)
//                if (newValue != oldValue) {
//                    val bounds: FloatArray = floatArrayOf(minValue, maxValue)
//                    mCameraEngine.setExposureCorrection(newValue, bounds, points, true)
//                }
//            }
//            FILTER_CONTROL_1 -> if (getFilter() is OneParameterFilter) {
//                val filter: OneParameterFilter = getFilter() as OneParameterFilter
//                oldValue = filter.getParameter1()
//                newValue = source.computeValue(oldValue, 0, 1)
//                if (newValue != oldValue) {
//                    filter.setParameter1(newValue)
//                }
//            }
//            FILTER_CONTROL_2 -> if (getFilter() is TwoParameterFilter) {
//                val filter: TwoParameterFilter = getFilter() as TwoParameterFilter
//                oldValue = filter.getParameter2()
//                newValue = source.computeValue(oldValue, 0, 1)
//                if (newValue != oldValue) {
//                    filter.setParameter2(newValue)
//                }
//            }
//        }
//    }
//    //endregion
//    //region Lifecycle APIs
//    /**
//     * Sets permissions flag if you want enable auto check permissions or disable it.
//     * @param requestPermissions - true: auto check permissions enabled, false: auto check permissions disabled.
//     */
//    fun setRequestPermissions(requestPermissions: Boolean) {
//        mRequestPermissions = requestPermissions
//    }
//
//    /**
//     * Returns whether the camera engine has started.
//     * @return whether the camera has started
//     */
//    val isOpened: Boolean
//        get() = (mCameraEngine.getState().isAtLeast(CameraState.ENGINE)
//                && mCameraEngine.getTargetState().isAtLeast(CameraState.ENGINE))
//    private val isClosed: Boolean
//        private get() {
//            return (mCameraEngine.getState() === CameraState.OFF
//                    && !mCameraEngine.isChangingState())
//        }
//
//    /**
//     * Sets the lifecycle owner for this view. This means you don't need
//     * to call [.open], [.close] or [.destroy] at all.
//     *
//     * If you want that lifecycle stopped controlling the state of the camera,
//     * pass null in this method.
//     *
//     * @param owner the owner activity or fragment
//     */
//    fun setLifecycleOwner(owner: LifecycleOwner?) {
//        if (owner == null) {
//            clearLifecycleObserver()
//        } else {
//            clearLifecycleObserver()
//            mLifecycle = owner.lifecycle
//            mLifecycle!!.addObserver(this)
//        }
//    }
//
//    private fun clearLifecycleObserver() {
//        if (mLifecycle != null) {
//            mLifecycle!!.removeObserver(this)
//            mLifecycle = null
//        }
//    }
//
//    /**
//     * Starts the camera preview, if not started already.
//     * This should be called onResume(), or when you are ready with permissions.
//     */
//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//    fun open() {
//        if (mInEditor) return
//        if (mCameraPreview != null) mCameraPreview.onResume()
//        if (checkPermissions(getAudio())) {
//            // Update display orientation for current CameraEngine
//            mOrientationHelper.enable()
//            mCameraEngine.getAngles().setDisplayOffset(mOrientationHelper.getLastDisplayOffset())
//            mCameraEngine.start()
//        }
//    }
//
//    /**
//     * Checks that we have appropriate permissions.
//     * This means checking that we have audio permissions if audio = Audio.ON.
//     * @param audio the audio setting to be checked
//     * @return true if we can go on, false otherwise.
//     */
//    @SuppressLint("NewApi")
//    protected fun checkPermissions(audio: Audio): Boolean {
//        checkPermissionsManifestOrThrow(audio)
//        // Manifest is OK at this point. Let's check runtime permissions.
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
//        val c: Context = context
//        var needsCamera: Boolean = true
//        var needsAudio: Boolean =
//            (audio === Audio.ON) || (audio === Audio.MONO) || (audio === Audio.STEREO)
//        needsCamera = needsCamera && c.checkSelfPermission(Manifest.permission.CAMERA)
//        != PackageManager.PERMISSION_GRANTED
//        needsAudio = needsAudio && c.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
//        != PackageManager.PERMISSION_GRANTED
//        if (!needsCamera && !needsAudio) {
//            return true
//        } else if (mRequestPermissions) {
//            requestPermissions(needsCamera, needsAudio)
//            return false
//        } else {
//            return false
//        }
//    }
//
//    /**
//     * If audio is on we will ask for RECORD_AUDIO permission.
//     * If the developer did not add this to its manifest, throw and fire warnings.
//     */
//    private fun checkPermissionsManifestOrThrow(audio: Audio) {
//        if ((audio === Audio.ON) || (audio === Audio.MONO) || (audio === Audio.STEREO)) {
//            try {
//                val manager: PackageManager = context.packageManager
//                val info: PackageInfo = manager.getPackageInfo(
//                    context.packageName,
//                    PackageManager.GET_PERMISSIONS
//                )
//                for (requestedPermission: String in info.requestedPermissions) {
//                    if ((requestedPermission == Manifest.permission.RECORD_AUDIO)) {
//                        return
//                    }
//                }
//                val message: String = LOG.e(
//                    "Permission error: when audio is enabled (Audio.ON)" +
//                            " the RECORD_AUDIO permission should be added to the app manifest file."
//                )
//                throw IllegalStateException(message)
//            } catch (e: PackageManager.NameNotFoundException) {
//                // Not possible.
//            }
//        }
//    }
//
//    /**
//     * Stops the current preview, if any was started.
//     * This should be called onPause().
//     */
//    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//    fun close() {
//        if (mInEditor) return
//        mOrientationHelper.disable()
//        mCameraEngine.stop(false)
//        if (mCameraPreview != null) mCameraPreview.onPause()
//    }
//
//    /**
//     * Destroys this instance, releasing immediately
//     * the camera resource.
//     */
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun destroy() {
//        if (mInEditor) return
//        clearCameraListeners()
//        clearFrameProcessors()
//        mCameraEngine.destroy(true)
//        if (mCameraPreview != null) mCameraPreview.onDestroy()
//    }
//    //endregion
//    //region Public APIs for controls
//    /**
//     * Sets the experimental flag which occasionally can enable
//     * new, unstable beta features.
//     * @param experimental true to enable new features
//     */
//    fun setExperimental(experimental: Boolean) {
//        mExperimental = experimental
//    }
//
//    /**
//     * Shorthand for the appropriate set* method.
//     * For example, if control is a [Grid], this calls [.setGrid].
//     *
//     * @param control desired value
//     */
//    fun set(control: Control) {
//        if (control is Audio) {
//            setAudio(control as Audio)
//        } else if (control is Facing) {
//            setFacing(control as Facing)
//        } else if (control is Flash) {
//            setFlash(control as Flash)
//        } else if (control is Grid) {
//            setGrid(control as Grid)
//        } else if (control is Hdr) {
//            setHdr(control as Hdr)
//        } else if (control is Mode) {
//            setMode(control as Mode)
//        } else if (control is WhiteBalance) {
//            setWhiteBalance(control as WhiteBalance)
//        } else if (control is VideoCodec) {
//            setVideoCodec(control as VideoCodec)
//        } else if (control is AudioCodec) {
//            setAudioCodec(control as AudioCodec)
//        } else if (control is Preview) {
//            preview = control as Preview
//        } else if (control is Engine) {
//            setEngine(control as Engine)
//        } else if (control is PictureFormat) {
//            setPictureFormat(control as PictureFormat)
//        }
//    }
//
//    /**
//     * Shorthand for the appropriate get* method.
//     * For example, if control class is a [Grid], this calls [.getGrid].
//     *
//     * @param controlClass desired value class
//     * @param <T> the class type
//     * @return the control
//    </T> */
//    operator fun <T : Control?> get(controlClass: Class<T>): T {
//        if (controlClass == Audio::class.java) {
//            return getAudio()
//        } else if (controlClass == Facing::class.java) {
//            return getFacing()
//        } else if (controlClass == Flash::class.java) {
//            return getFlash()
//        } else if (controlClass == Grid::class.java) {
//            return getGrid()
//        } else if (controlClass == Hdr::class.java) {
//            return getHdr()
//        } else if (controlClass == Mode::class.java) {
//            return getMode()
//        } else if (controlClass == WhiteBalance::class.java) {
//            return getWhiteBalance()
//        } else if (controlClass == VideoCodec::class.java) {
//            return getVideoCodec()
//        } else if (controlClass == AudioCodec::class.java) {
//            return getAudioCodec()
//        } else if (controlClass == Preview::class.java) {
//            return preview
//        } else if (controlClass == Engine::class.java) {
//            return getEngine()
//        } else if (controlClass == PictureFormat::class.java) {
//            return getPictureFormat()
//        } else {
//            throw IllegalArgumentException("Unknown control class: $controlClass")
//        }
//    }
//    /**
//     * Returns the current preview control.
//     *
//     * @see .setPreview
//     * @return the current preview control
//     */// Null the preview: will create another when re-attaching.
//    /**
//     * Controls the preview engine. Should only be called
//     * if this CameraView was never added to any window
//     * (like if you created it programmatically).
//     * Otherwise, it has no effect.
//     *
//     * @see Preview.SURFACE
//     *
//     * @see Preview.TEXTURE
//     *
//     * @see Preview.GL_SURFACE
//     *
//     *
//     * @param preview desired preview engine
//     */
//    var preview: Preview
//        get() {
//            return mPreview
//        }
//
//    /**
//     * Controls the core engine. Should only be called
//     * if this CameraView is closed (open() was never called).
//     * Otherwise, it has no effect.
//     *
//     * @see Engine.CAMERA1
//     *
//     * @see Engine.CAMERA2
//     *
//     *
//     * @param engine desired engine
//     */
//    fun setEngine(engine: Engine) {
//        if (!isClosed) return
//        mEngine = engine
//        val oldEngine: CameraEngine? = mCameraEngine
//        doInstantiateEngine()
//        if (mCameraPreview != null) mCameraEngine.setPreview(mCameraPreview)
//
//        // Set again all parameters
//        setFacing(oldEngine.getFacing())
//        setFlash(oldEngine.getFlash())
//        setMode(oldEngine.getMode())
//        setWhiteBalance(oldEngine.getWhiteBalance())
//        setHdr(oldEngine.getHdr())
//        setAudio(oldEngine.getAudio())
//        setAudioBitRate(oldEngine.getAudioBitRate())
//        setAudioCodec(oldEngine.getAudioCodec())
//        setPictureSize(oldEngine.getPictureSizeSelector())
//        setPictureFormat(oldEngine.getPictureFormat())
//        setVideoSize(oldEngine.getVideoSizeSelector())
//        setVideoCodec(oldEngine.getVideoCodec())
//        setVideoMaxSize(oldEngine.getVideoMaxSize())
//        setVideoMaxDuration(oldEngine.getVideoMaxDuration())
//        setVideoBitRate(oldEngine.getVideoBitRate())
//        setAutoFocusResetDelay(oldEngine.getAutoFocusResetDelay())
//        setPreviewFrameRate(oldEngine.getPreviewFrameRate())
//        setPreviewFrameRateExact(oldEngine.getPreviewFrameRateExact())
//        setSnapshotMaxWidth(oldEngine.getSnapshotMaxWidth())
//        setSnapshotMaxHeight(oldEngine.getSnapshotMaxHeight())
//        setFrameProcessingMaxWidth(oldEngine.getFrameProcessingMaxWidth())
//        setFrameProcessingMaxHeight(oldEngine.getFrameProcessingMaxHeight())
//        setFrameProcessingFormat(0 /* this is very engine specific, so do not pass */)
//        setFrameProcessingPoolSize(oldEngine.getFrameProcessingPoolSize())
//        mCameraEngine.setHasFrameProcessors(!mFrameProcessors.isEmpty())
//    }
//
//    /**
//     * Returns the current engine control.
//     *
//     * @see .setEngine
//     * @return the current engine control
//     */
//    fun getEngine(): Engine {
//        return mEngine
//    }
//
//    /**
//     * Returns a [CameraOptions] instance holding supported options for this camera
//     * session. This might change over time. It's better to hold a reference from
//     * [CameraListener.onCameraOpened].
//     *
//     * @return an options map, or null if camera was not opened
//     */
//    fun getCameraOptions(): CameraOptions? {
//        return mCameraEngine.getCameraOptions()
//    }
//
//    /**
//     * Sets exposure adjustment, in EV stops. A positive value will mean brighter picture.
//     *
//     * If camera is not opened, this will have no effect.
//     * If [CameraOptions.isExposureCorrectionSupported] is false, this will have no effect.
//     * The provided value should be between the bounds returned by [CameraOptions], or it will
//     * be capped.
//     *
//     * @see CameraOptions.getExposureCorrectionMinValue
//     * @see CameraOptions.getExposureCorrectionMaxValue
//     * @param EVvalue exposure correction value.
//     */
//    fun setExposureCorrection(EVvalue: Float) {
//        var EVvalue: Float = EVvalue
//        val options: CameraOptions? = getCameraOptions()
//        if (options != null) {
//            val min: Float = options.getExposureCorrectionMinValue()
//            val max: Float = options.getExposureCorrectionMaxValue()
//            if (EVvalue < min) EVvalue = min
//            if (EVvalue > max) EVvalue = max
//            val bounds: FloatArray = floatArrayOf(min, max)
//            mCameraEngine.setExposureCorrection(EVvalue, bounds, null, false)
//        }
//    }
//
//    /**
//     * Returns the current exposure correction value, typically 0
//     * at start-up.
//     * @return the current exposure correction value
//     */
//    fun getExposureCorrection(): Float {
//        return mCameraEngine.getExposureCorrectionValue()
//    }
//
//    /**
//     * Sets a zoom value. This is not guaranteed to be supported by the current device,
//     * but you can take a look at [CameraOptions.isZoomSupported].
//     * This will have no effect if called before the camera is opened.
//     *
//     * Zoom value should be between 0 and 1, where 1 will be the maximum available zoom.
//     * If it's not, it will be capped.
//     *
//     * @param zoom value in [0,1]
//     */
//    fun setZoom(zoom: Float) {
//        var zoom: Float = zoom
//        if (zoom < 0) zoom = 0f
//        if (zoom > 1) zoom = 1f
//        mCameraEngine.setZoom(zoom, null, false)
//    }
//
//    /**
//     * Returns the current zoom value, something between 0 and 1.
//     * @return the current zoom value
//     */
//    fun getZoom(): Float {
//        return mCameraEngine.getZoomValue()
//    }
//
//    /**
//     * Controls the grids to be drawn over the current layout.
//     *
//     * @see Grid.OFF
//     *
//     * @see Grid.DRAW_3X3
//     *
//     * @see Grid.DRAW_4X4
//     *
//     * @see Grid.DRAW_PHI
//     *
//     *
//     * @param gridMode desired grid mode
//     */
//    fun setGrid(gridMode: Grid) {
//        mGridLinesLayout.setGridMode(gridMode)
//    }
//
//    /**
//     * Gets the current grid mode.
//     * @return the current grid mode
//     */
//    fun getGrid(): Grid {
//        return mGridLinesLayout.getGridMode()
//    }
//
//    /**
//     * Controls the color of the grid lines that will be drawn
//     * over the current layout.
//     *
//     * @param color a resolved color
//     */
//    fun setGridColor(@ColorInt color: Int) {
//        mGridLinesLayout.setGridColor(color)
//    }
//
//    /**
//     * Returns the current grid color.
//     * @return the current grid color
//     */
//    fun getGridColor(): Int {
//        return mGridLinesLayout.getGridColor()
//    }
//
//    /**
//     * Controls the grids to be drawn over the current layout.
//     *
//     * @see Hdr.OFF
//     *
//     * @see Hdr.ON
//     *
//     *
//     * @param hdr desired hdr value
//     */
//    fun setHdr(hdr: Hdr) {
//        mCameraEngine.setHdr(hdr)
//    }
//
//    /**
//     * Gets the current hdr value.
//     * @return the current hdr value
//     */
//    fun getHdr(): Hdr {
//        return mCameraEngine.getHdr()
//    }
//
//    /**
//     * Set location coordinates to be found later in the EXIF header
//     *
//     * @param latitude current latitude
//     * @param longitude current longitude
//     */
//    fun setLocation(latitude: Double, longitude: Double) {
//        val location: Location = Location("Unknown")
//        location.time = System.currentTimeMillis()
//        location.altitude = 0.0
//        location.latitude = latitude
//        location.longitude = longitude
//        mCameraEngine.setLocation(location)
//    }
//
//    /**
//     * Set location values to be found later in the EXIF header
//     *
//     * @param location current location
//     */
//    fun setLocation(location: Location?) {
//        mCameraEngine.setLocation(location)
//    }
//
//    /**
//     * Retrieves the location previously applied with setLocation().
//     *
//     * @return the current location, if any.
//     */
//    fun getLocation(): Location? {
//        return mCameraEngine.getLocation()
//    }
//
//    /**
//     * Sets desired white balance to current camera session.
//     *
//     * @see WhiteBalance.AUTO
//     *
//     * @see WhiteBalance.INCANDESCENT
//     *
//     * @see WhiteBalance.FLUORESCENT
//     *
//     * @see WhiteBalance.DAYLIGHT
//     *
//     * @see WhiteBalance.CLOUDY
//     *
//     *
//     * @param whiteBalance desired white balance behavior.
//     */
//    fun setWhiteBalance(whiteBalance: WhiteBalance) {
//        mCameraEngine.setWhiteBalance(whiteBalance)
//    }
//
//    /**
//     * Returns the current white balance behavior.
//     * @return white balance value.
//     */
//    fun getWhiteBalance(): WhiteBalance {
//        return mCameraEngine.getWhiteBalance()
//    }
//
//    /**
//     * Sets which camera sensor should be used.
//     *
//     * @see Facing.FRONT
//     *
//     * @see Facing.BACK
//     *
//     *
//     * @param facing a facing value.
//     */
//    fun setFacing(facing: Facing) {
//        mCameraEngine.setFacing(facing)
//    }
//
//    /**
//     * Gets the facing camera currently being used.
//     * @return a facing value.
//     */
//    fun getFacing(): Facing {
//        return mCameraEngine.getFacing()
//    }
//
//    /**
//     * Toggles the facing value between [Facing.BACK]
//     * and [Facing.FRONT].
//     *
//     * @return the new facing value
//     */
//    fun toggleFacing(): Facing {
//        val facing: Facing = mCameraEngine.getFacing()
//        when (facing) {
//            BACK -> setFacing(Facing.FRONT)
//            FRONT -> setFacing(Facing.BACK)
//        }
//        return mCameraEngine.getFacing()
//    }
//
//    /**
//     * Sets the flash mode.
//     *
//     * @see Flash.OFF
//     *
//     * @see Flash.ON
//     *
//     * @see Flash.AUTO
//     *
//     * @see Flash.TORCH
//     *
//     *
//     * @param flash desired flash mode.
//     */
//    fun setFlash(flash: Flash) {
//        mCameraEngine.setFlash(flash)
//    }
//
//    /**
//     * Gets the current flash mode.
//     * @return a flash mode
//     */
//    fun getFlash(): Flash {
//        return mCameraEngine.getFlash()
//    }
//
//    /**
//     * Controls the audio mode.
//     *
//     * @see Audio.OFF
//     *
//     * @see Audio.ON
//     *
//     * @see Audio.MONO
//     *
//     * @see Audio.STEREO
//     *
//     *
//     * @param audio desired audio value
//     */
//    fun setAudio(audio: Audio) {
//        if (audio === getAudio() || isClosed) {
//            // Check did took place, or will happen on start().
//            mCameraEngine.setAudio(audio)
//        } else if (checkPermissions(audio)) {
//            // Camera is running. Pass.
//            mCameraEngine.setAudio(audio)
//        } else {
//            // This means that the audio permission is being asked.
//            // Stop the camera so it can be restarted by the developer onPermissionResult.
//            // Developer must also set the audio value again...
//            // Not ideal but good for now.
//            close()
//        }
//    }
//
//    /**
//     * Gets the current audio value.
//     * @return the current audio value
//     */
//    fun getAudio(): Audio {
//        return mCameraEngine.getAudio()
//    }
//
//    /**
//     * Sets an [AutoFocusMarker] to be notified of metering start, end and fail events
//     * so that it can draw elements on screen.
//     *
//     * @param autoFocusMarker the marker, or null
//     */
//    fun setAutoFocusMarker(autoFocusMarker: AutoFocusMarker?) {
//        mAutoFocusMarker = autoFocusMarker
//        mMarkerLayout.onMarker(MarkerLayout.TYPE_AUTOFOCUS, autoFocusMarker)
//    }
//
//    /**
//     * Sets the current delay in milliseconds to reset the focus after a metering event.
//     *
//     * @param delayMillis desired delay (in milliseconds). If the delay
//     * is less than or equal to 0 or equal to Long.MAX_VALUE,
//     * the values will not be reset.
//     */
//    fun setAutoFocusResetDelay(delayMillis: Long) {
//        mCameraEngine.setAutoFocusResetDelay(delayMillis)
//    }
//
//    /**
//     * Returns the current delay in milliseconds to reset the focus after a metering event.
//     *
//     * @return the current reset delay in milliseconds
//     */
//    fun getAutoFocusResetDelay(): Long {
//        return mCameraEngine.getAutoFocusResetDelay()
//    }
//
//    /**
//     * Starts a 3A touch metering process at the given coordinates, with respect
//     * to the view width and height.
//     *
//     * @param x should be between 0 and getWidth()
//     * @param y should be between 0 and getHeight()
//     */
//    fun startAutoFocus(x: Float, y: Float) {
//        if (x < 0 || x > width) {
//            throw IllegalArgumentException("x should be >= 0 and <= getWidth()")
//        }
//        if (y < 0 || y > height) {
//            throw IllegalArgumentException("y should be >= 0 and <= getHeight()")
//        }
//        val size: Size = Size(width, height)
//        val point: PointF = PointF(x, y)
//        val regions: MeteringRegions = MeteringRegions.fromPoint(size, point)
//        mCameraEngine.startAutoFocus(null, regions, point)
//    }
//
//    /**
//     * Starts a 3A touch metering process at the given coordinates, with respect
//     * to the view width and height.
//     *
//     * @param region should be between 0 and getWidth() / getHeight()
//     */
//    fun startAutoFocus(region: RectF) {
//        val full: RectF = RectF(0, 0, width.toFloat(), height.toFloat())
//        if (!full.contains(region)) {
//            throw IllegalArgumentException("Region is out of view bounds! $region")
//        }
//        val size: Size = Size(width, height)
//        val regions: MeteringRegions = MeteringRegions.fromArea(size, region)
//        mCameraEngine.startAutoFocus(
//            null, regions,
//            PointF(region.centerX(), region.centerY())
//        )
//    }
//
//    /**
//     * **ADVANCED FEATURE** - sets a size selector for the preview stream.
//     * The [SizeSelector] will be invoked with the list of available sizes, and the first
//     * acceptable size will be accepted and passed to the internal engine and surface.
//     *
//     * This is typically NOT NEEDED. The default size selector is already smart enough to respect
//     * the picture/video output aspect ratio, and be bigger than the surface so that there is no
//     * upscaling. If all you want is set an aspect ratio, use [.setPictureSize]
//     * and [.setVideoSize].
//     *
//     * When stream size changes, the [CameraView] is remeasured so any WRAP_CONTENT dimension
//     * is recomputed accordingly.
//     *
//     * See the [SizeSelectors] class for handy utilities for creating selectors.
//     *
//     * @param selector a size selector
//     */
//    fun setPreviewStreamSize(selector: SizeSelector) {
//        mCameraEngine.setPreviewStreamSizeSelector(selector)
//    }
//
//    /**
//     * Set the current session type to either picture or video.
//     *
//     * @see Mode.PICTURE
//     *
//     * @see Mode.VIDEO
//     *
//     *
//     * @param mode desired session type.
//     */
//    fun setMode(mode: Mode) {
//        mCameraEngine.setMode(mode)
//    }
//
//    /**
//     * Gets the current mode.
//     * @return the current mode
//     */
//    fun getMode(): Mode {
//        return mCameraEngine.getMode()
//    }
//
//    /**
//     * Sets a capture size selector for picture mode.
//     * The [SizeSelector] will be invoked with the list of available sizes, and the first
//     * acceptable size will be accepted and passed to the internal engine.
//     * See the [SizeSelectors] class for handy utilities for creating selectors.
//     *
//     * @param selector a size selector
//     */
//    fun setPictureSize(selector: SizeSelector) {
//        mCameraEngine.setPictureSizeSelector(selector)
//    }
//
//    /**
//     * Whether the engine should perform a metering sequence before taking pictures requested
//     * with [.takePicture]. A metering sequence includes adjusting focus, exposure
//     * and white balance to ensure a good quality of the result.
//     *
//     * When this parameter is true, the quality of the picture increases, but the latency
//     * increases as well. Defaults to true.
//     *
//     * This is a CAMERA2 only API. On CAMERA1, picture metering is always enabled.
//     *
//     * @see .setPictureSnapshotMetering
//     * @param enable true to enable
//     */
//    fun setPictureMetering(enable: Boolean) {
//        mCameraEngine.setPictureMetering(enable)
//    }
//
//    /**
//     * Whether the engine should perform a metering sequence before taking pictures requested
//     * with [.takePicture]. See [.setPictureMetering].
//     *
//     * @see .setPictureMetering
//     * @return true if picture metering is enabled
//     */
//    fun getPictureMetering(): Boolean {
//        return mCameraEngine.getPictureMetering()
//    }
//
//    /**
//     * Whether the engine should perform a metering sequence before taking pictures requested
//     * with [.takePictureSnapshot]. A metering sequence includes adjusting focus,
//     * exposure and white balance to ensure a good quality of the result.
//     *
//     * When this parameter is true, the quality of the picture increases, but the latency
//     * increases as well. To keep snapshots fast, this defaults to false.
//     *
//     * This is a CAMERA2 only API. On CAMERA1, picture snapshot metering is always disabled.
//     *
//     * @see .setPictureMetering
//     * @param enable true to enable
//     */
//    fun setPictureSnapshotMetering(enable: Boolean) {
//        mCameraEngine.setPictureSnapshotMetering(enable)
//    }
//
//    /**
//     * Whether the engine should perform a metering sequence before taking pictures requested
//     * with [.takePictureSnapshot]. See [.setPictureSnapshotMetering].
//     *
//     * @see .setPictureSnapshotMetering
//     * @return true if picture metering is enabled
//     */
//    fun getPictureSnapshotMetering(): Boolean {
//        return mCameraEngine.getPictureSnapshotMetering()
//    }
//
//    /**
//     * Sets the format for pictures taken with [.takePicture]. This format does not apply
//     * to picture snapshots taken with [.takePictureSnapshot].
//     * The [PictureFormat.JPEG] is always supported - for other values, please check
//     * the [CameraOptions.getSupportedPictureFormats] value.
//     *
//     * @param pictureFormat new format
//     */
//    fun setPictureFormat(pictureFormat: PictureFormat) {
//        mCameraEngine.setPictureFormat(pictureFormat)
//    }
//
//    /**
//     * Returns the current picture format.
//     * @see .setPictureFormat
//     * @return the picture format
//     */
//    fun getPictureFormat(): PictureFormat {
//        return mCameraEngine.getPictureFormat()
//    }
//
//    /**
//     * Sets a capture size selector for video mode.
//     * The [SizeSelector] will be invoked with the list of available sizes, and the first
//     * acceptable size will be accepted and passed to the internal engine.
//     * See the [SizeSelectors] class for handy utilities for creating selectors.
//     *
//     * @param selector a size selector
//     */
//    fun setVideoSize(selector: SizeSelector) {
//        mCameraEngine.setVideoSizeSelector(selector)
//    }
//
//    /**
//     * Sets the bit rate in bits per second for video capturing.
//     * Will be used by both [.takeVideo] and [.takeVideoSnapshot].
//     *
//     * @param bitRate desired bit rate
//     */
//    fun setVideoBitRate(bitRate: Int) {
//        mCameraEngine.setVideoBitRate(bitRate)
//    }
//
//    /**
//     * Returns the current video bit rate.
//     * @return current bit rate
//     */
//    fun getVideoBitRate(): Int {
//        return mCameraEngine.getVideoBitRate()
//    }
//
//    /**
//     * A flag to control the behavior when calling [.setPreviewFrameRate].
//     *
//     * If the value is set to true, [.setPreviewFrameRate] will choose the preview
//     * frame range as close to the desired new frame rate as possible. Which mean it may choose a
//     * narrow range around the desired frame rate. Note: This option will give you as exact fps as
//     * you want but the sensor will have less freedom when adapting the exposure to the environment,
//     * which may lead to dark preview.
//     *
//     * If the value is set to false, [.setPreviewFrameRate] will choose as broad range
//     * as it can.
//     *
//     * @param videoFrameRateExact whether want a more exact preview frame range
//     *
//     * @see .setPreviewFrameRate
//     */
//    fun setPreviewFrameRateExact(videoFrameRateExact: Boolean) {
//        mCameraEngine.setPreviewFrameRateExact(videoFrameRateExact)
//    }
//
//    /**
//     * Returns whether we want to set preview fps as exact as we set through
//     * [.setPreviewFrameRate].
//     *
//     * @see .setPreviewFrameRateExact
//     * @see .setPreviewFrameRate
//     * @return current option
//     */
//    fun getPreviewFrameRateExact(): Boolean {
//        return mCameraEngine.getPreviewFrameRateExact()
//    }
//
//    /**
//     * Sets the preview frame rate in frames per second.
//     * This rate will be used, for example, by the frame processor and in video
//     * snapshot taken through [.takeVideo].
//     *
//     * A value of 0F will restore the rate to a default value.
//     *
//     * @param frameRate desired frame rate
//     */
//    fun setPreviewFrameRate(frameRate: Float) {
//        mCameraEngine.setPreviewFrameRate(frameRate)
//    }
//
//    /**
//     * Returns the current preview frame rate.
//     * This can return 0F if no frame rate was set.
//     *
//     * @see .setPreviewFrameRate
//     * @return current frame rate
//     */
//    fun getPreviewFrameRate(): Float {
//        return mCameraEngine.getPreviewFrameRate()
//    }
//
//    /**
//     * Sets the bit rate in bits per second for audio capturing.
//     * Will be used by both [.takeVideo] and [.takeVideoSnapshot].
//     *
//     * @param bitRate desired bit rate
//     */
//    fun setAudioBitRate(bitRate: Int) {
//        mCameraEngine.setAudioBitRate(bitRate)
//    }
//
//    /**
//     * Returns the current audio bit rate.
//     * @return current bit rate
//     */
//    fun getAudioBitRate(): Int {
//        return mCameraEngine.getAudioBitRate()
//    }
//
//    /**
//     * Sets the encoder for audio recordings.
//     * Defaults to [AudioCodec.DEVICE_DEFAULT].
//     *
//     * @see AudioCodec.DEVICE_DEFAULT
//     *
//     * @see AudioCodec.AAC
//     *
//     * @see AudioCodec.HE_AAC
//     *
//     * @see AudioCodec.AAC_ELD
//     *
//     *
//     * @param codec requested audio codec
//     */
//    fun setAudioCodec(codec: AudioCodec) {
//        mCameraEngine.setAudioCodec(codec)
//    }
//
//    /**
//     * Gets the current encoder for audio recordings.
//     * @return the current audio codec
//     */
//    fun getAudioCodec(): AudioCodec {
//        return mCameraEngine.getAudioCodec()
//    }
//
//    /**
//     * Adds a [CameraListener] instance to be notified of all
//     * interesting events that happen during the camera lifecycle.
//     *
//     * @param cameraListener a listener for events.
//     */
//    fun addCameraListener(cameraListener: CameraListener) {
//        mListeners.add(cameraListener)
//    }
//
//    /**
//     * Remove a [CameraListener] that was previously registered.
//     *
//     * @param cameraListener a listener for events.
//     */
//    fun removeCameraListener(cameraListener: CameraListener) {
//        mListeners.remove(cameraListener)
//    }
//
//    /**
//     * Clears the list of [CameraListener] that are registered
//     * to camera events.
//     */
//    fun clearCameraListeners() {
//        mListeners.clear()
//    }
//
//    /**
//     * Asks the camera to capture an image of the current scene.
//     * This will trigger [CameraListener.onPictureTaken] if a listener
//     * was registered.
//     *
//     * @see .takePictureSnapshot
//     */
//    fun takePicture() {
//        val stub: PictureResult.Stub = Stub()
//        mCameraEngine.takePicture(stub)
//    }
//
//    /**
//     * Asks the camera to capture a snapshot of the current preview.
//     * This eventually triggers [CameraListener.onPictureTaken] if a listener
//     * was registered.
//     *
//     * The difference with [.takePicture] is that this capture is faster, so it might be
//     * better on slower cameras, though the result can be generally blurry or low quality.
//     *
//     * @see .takePicture
//     */
//    fun takePictureSnapshot() {
//        val stub: PictureResult.Stub = Stub()
//        mCameraEngine.takePictureSnapshot(stub)
//    }
//
//    /**
//     * Starts recording a video. Video will be written to the given file,
//     * so callers should ensure they have appropriate permissions to write to the file.
//     *
//     * @param file a file where the video will be saved
//     */
//    fun takeVideo(file: File) {
//        takeVideo(file, null)
//    }
//
//    /**
//     * Starts recording a video. Video will be written to the given file,
//     * so callers should ensure they have appropriate permissions to write to the file.
//     *
//     * @param fileDescriptor a file descriptor where the video will be saved
//     */
//    fun takeVideo(fileDescriptor: FileDescriptor) {
//        takeVideo(null, fileDescriptor)
//    }
//
//    private fun takeVideo(file: File?, fileDescriptor: FileDescriptor?) {
//        val stub: VideoResult.Stub = Stub()
//        if (file != null) {
//            mCameraEngine.takeVideo(stub, file, null)
//        } else if (fileDescriptor != null) {
//            mCameraEngine.takeVideo(stub, null, fileDescriptor)
//        } else {
//            throw IllegalStateException("file and fileDescriptor are both null.")
//        }
//        mUiHandler!!.post(object : Runnable {
//            override fun run() {
//                mKeepScreenOn = keepScreenOn
//                if (!mKeepScreenOn) keepScreenOn = true
//            }
//        })
//    }
//
//    /**
//     * Starts recording a fast, low quality video snapshot. Video will be written to the given file,
//     * so callers should ensure they have appropriate permissions to write to the file.
//     *
//     * Throws an exception if API level is below 18, or if the preview being used is not
//     * [Preview.GL_SURFACE].
//     *
//     * @param file a file where the video will be saved
//     */
//    fun takeVideoSnapshot(file: File) {
//        val stub: VideoResult.Stub = Stub()
//        mCameraEngine.takeVideoSnapshot(stub, file)
//        mUiHandler!!.post(object : Runnable {
//            override fun run() {
//                mKeepScreenOn = keepScreenOn
//                if (!mKeepScreenOn) keepScreenOn = true
//            }
//        })
//    }
//
//    /**
//     * Starts recording a video. Video will be written to the given file,
//     * so callers should ensure they have appropriate permissions to write to the file.
//     * Recording will be automatically stopped after the given duration, overriding
//     * temporarily any duration limit set by [.setVideoMaxDuration].
//     *
//     * @param file a file where the video will be saved
//     * @param durationMillis recording max duration
//     */
//    fun takeVideo(file: File, durationMillis: Int) {
//        takeVideo(file, null, durationMillis)
//    }
//
//    /**
//     * Starts recording a video. Video will be written to the given file,
//     * so callers should ensure they have appropriate permissions to write to the file.
//     * Recording will be automatically stopped after the given duration, overriding
//     * temporarily any duration limit set by [.setVideoMaxDuration].
//     *
//     * @param fileDescriptor a file descriptor where the video will be saved
//     * @param durationMillis recording max duration
//     */
//    fun takeVideo(fileDescriptor: FileDescriptor, durationMillis: Int) {
//        takeVideo(null, fileDescriptor, durationMillis)
//    }
//
//    private fun takeVideo(
//        file: File?, fileDescriptor: FileDescriptor?,
//        durationMillis: Int
//    ) {
//        val old: Int = getVideoMaxDuration()
//        addCameraListener(object : CameraListener() {
//            fun onVideoTaken(result: VideoResult) {
//                setVideoMaxDuration(old)
//                removeCameraListener(this)
//            }
//
//            fun onCameraError(exception: CameraException) {
//                super.onCameraError(exception)
//                if (exception.getReason() === CameraException.REASON_VIDEO_FAILED) {
//                    setVideoMaxDuration(old)
//                    removeCameraListener(this)
//                }
//            }
//        })
//        setVideoMaxDuration(durationMillis)
//        takeVideo(file, fileDescriptor)
//    }
//
//    /**
//     * Starts recording a fast, low quality video snapshot. Video will be written to the given file,
//     * so callers should ensure they have appropriate permissions to write to the file.
//     * Recording will be automatically stopped after the given duration, overriding
//     * temporarily any duration limit set by [.setVideoMaxDuration].
//     *
//     * Throws an exception if API level is below 18, or if the preview being used is not
//     * [Preview.GL_SURFACE].
//     *
//     * @param file a file where the video will be saved
//     * @param durationMillis recording max duration
//     */
//    fun takeVideoSnapshot(file: File, durationMillis: Int) {
//        val old: Int = getVideoMaxDuration()
//        addCameraListener(object : CameraListener() {
//            fun onVideoTaken(result: VideoResult) {
//                setVideoMaxDuration(old)
//                removeCameraListener(this)
//            }
//
//            fun onCameraError(exception: CameraException) {
//                super.onCameraError(exception)
//                if (exception.getReason() === CameraException.REASON_VIDEO_FAILED) {
//                    setVideoMaxDuration(old)
//                    removeCameraListener(this)
//                }
//            }
//        })
//        setVideoMaxDuration(durationMillis)
//        takeVideoSnapshot(file)
//    }
//    // TODO: pauseVideo and resumeVideo? There is mediarecorder.pause(), but API 24...
//    /**
//     * Stops capturing video or video snapshots being recorded, if there was any.
//     * This will fire [CameraListener.onVideoTaken].
//     */
//    fun stopVideo() {
//        mCameraEngine.stopVideo()
//        mUiHandler!!.post(object : Runnable {
//            override fun run() {
//                if (keepScreenOn != mKeepScreenOn) keepScreenOn = mKeepScreenOn
//            }
//        })
//    }
//
//    /**
//     * Sets the max width for snapshots taken with [.takePictureSnapshot] or
//     * [.takeVideoSnapshot]. If the snapshot width exceeds this value, the snapshot
//     * will be scaled down to match this constraint.
//     *
//     * @param maxWidth max width for snapshots
//     */
//    fun setSnapshotMaxWidth(maxWidth: Int) {
//        mCameraEngine.setSnapshotMaxWidth(maxWidth)
//    }
//
//    /**
//     * Sets the max height for snapshots taken with [.takePictureSnapshot] or
//     * [.takeVideoSnapshot]. If the snapshot height exceeds this value, the snapshot
//     * will be scaled down to match this constraint.
//     *
//     * @param maxHeight max height for snapshots
//     */
//    fun setSnapshotMaxHeight(maxHeight: Int) {
//        mCameraEngine.setSnapshotMaxHeight(maxHeight)
//    }
//
//    /**
//     * The max width for snapshots.
//     * @see .setSnapshotMaxWidth
//     * @return max width
//     */
//    fun getSnapshotMaxWidth(): Int {
//        return mCameraEngine.getSnapshotMaxWidth()
//    }
//
//    /**
//     * The max height for snapshots.
//     * @see .setSnapshotMaxHeight
//     * @return max height
//     */
//    fun getSnapshotMaxHeight(): Int {
//        return mCameraEngine.getSnapshotMaxHeight()
//    }
//
//    /**
//     * Returns the size used for snapshots, or null if it hasn't been computed
//     * (for example if the surface is not ready). This is the preview size, rotated to match
//     * the output orientation, and cropped to the visible part.
//     *
//     * This also includes the [.setSnapshotMaxWidth] and
//     * [.setSnapshotMaxHeight] constraints.
//     *
//     * This does NOT include any constraints specific to video encoding, which are
//     * device specific and depend on the capabilities of the device codec.
//     *
//     * @return the size of snapshots
//     */
//    fun getSnapshotSize(): Size? {
//        if (width == 0 || height == 0) return null
//
//        // Get the preview size and crop according to the current view size.
//        // It's better to do calculations in the REF_VIEW reference, and then flip if needed.
//        val preview: Size? = mCameraEngine.getUncroppedSnapshotSize(Reference.VIEW)
//        if (preview == null) return null // Should never happen.
//        val viewRatio: AspectRatio = AspectRatio.of(width, height)
//        val crop: Rect = CropHelper.computeCrop(preview, viewRatio)
//        val cropSize: Size = Size(crop.width(), crop.height())
//        if (mCameraEngine.getAngles().flip(Reference.VIEW, Reference.OUTPUT)) {
//            return cropSize.flip()
//        } else {
//            return cropSize
//        }
//    }
//
//    /**
//     * Returns the size used for pictures taken with [.takePicture],
//     * or null if it hasn't been computed (for example if the surface is not ready),
//     * or null if we are in video mode.
//     *
//     * The size is rotated to match the output orientation.
//     *
//     * @return the size of pictures
//     */
//    fun getPictureSize(): Size? {
//        return mCameraEngine.getPictureSize(Reference.OUTPUT)
//    }
//
//    /**
//     * Returns the size used for videos taken with [.takeVideo],
//     * or null if it hasn't been computed (for example if the surface is not ready),
//     * or null if we are in picture mode.
//     *
//     * The size is rotated to match the output orientation.
//     *
//     * @return the size of videos
//     */
//    fun getVideoSize(): Size? {
//        return mCameraEngine.getVideoSize(Reference.OUTPUT)
//    }
//
//    // If we end up here, we're in M.
//    @TargetApi(Build.VERSION_CODES.M)
//    private fun requestPermissions(requestCamera: Boolean, requestAudio: Boolean) {
//        var activity: Activity? = null
//        var context: Context? = context
//        while (context is ContextWrapper) {
//            if (context is Activity) {
//                activity = context
//            }
//            context = context.baseContext
//        }
//        val permissions: MutableList<String> = ArrayList()
//        if (requestCamera) permissions.add(Manifest.permission.CAMERA)
//        if (requestAudio) permissions.add(Manifest.permission.RECORD_AUDIO)
//        if (activity != null) {
//            activity.requestPermissions(
//                permissions.toTypedArray(),
//                PERMISSION_REQUEST_CODE
//            )
//        }
//    }
//
//    @SuppressLint("NewApi")
//    private fun playSound(soundType: Int) {
//        if (mPlaySounds) {
//            if (mSound == null) mSound = MediaActionSound()
//            mSound!!.play(soundType)
//        }
//    }
//
//    /**
//     * Controls whether CameraView should play sound effects on certain
//     * events (picture taken, focus complete). Note that:
//     * - On API level &lt; 16, this flag is always false
//     * - Camera1 will always play the shutter sound when taking pictures
//     *
//     * @param playSounds whether to play sound effects
//     */
//    fun setPlaySounds(playSounds: Boolean) {
//        mPlaySounds = playSounds && Build.VERSION.SDK_INT >= 16
//        mCameraEngine.setPlaySounds(playSounds)
//    }
//
//    /**
//     * Gets the current sound effect behavior.
//     *
//     * @see .setPlaySounds
//     * @return whether sound effects are supported
//     */
//    fun getPlaySounds(): Boolean {
//        return mPlaySounds
//    }
//
//    /**
//     * Controls whether picture and video output should consider the current device orientation.
//     * For example, when true, if the user rotates the device before taking a picture, the picture
//     * will be rotated as well.
//     *
//     * @param useDeviceOrientation true to consider device orientation for outputs
//     */
//    fun setUseDeviceOrientation(useDeviceOrientation: Boolean) {
//        mUseDeviceOrientation = useDeviceOrientation
//    }
//
//    /**
//     * Gets the current behavior for considering the device orientation when returning picture
//     * or video outputs.
//     *
//     * @see .setUseDeviceOrientation
//     * @return whether we are using the device orientation for outputs
//     */
//    fun getUseDeviceOrientation(): Boolean {
//        return mUseDeviceOrientation
//    }
//
//    /**
//     * Sets the encoder for video recordings.
//     * Defaults to [VideoCodec.DEVICE_DEFAULT].
//     *
//     * @see VideoCodec.DEVICE_DEFAULT
//     *
//     * @see VideoCodec.H_263
//     *
//     * @see VideoCodec.H_264
//     *
//     *
//     * @param codec requested video codec
//     */
//    fun setVideoCodec(codec: VideoCodec) {
//        mCameraEngine.setVideoCodec(codec)
//    }
//
//    /**
//     * Gets the current encoder for video recordings.
//     * @return the current video codec
//     */
//    fun getVideoCodec(): VideoCodec {
//        return mCameraEngine.getVideoCodec()
//    }
//
//    /**
//     * Sets the maximum size in bytes for recorded video files.
//     * Once this size is reached, the recording will automatically stop.
//     * Defaults to unlimited size. Use 0 or negatives to disable.
//     *
//     * @param videoMaxSizeInBytes The maximum video size in bytes
//     */
//    fun setVideoMaxSize(videoMaxSizeInBytes: Long) {
//        mCameraEngine.setVideoMaxSize(videoMaxSizeInBytes)
//    }
//
//    /**
//     * Returns the maximum size in bytes for recorded video files, or 0
//     * if no size was set.
//     *
//     * @see .setVideoMaxSize
//     * @return the maximum size in bytes
//     */
//    fun getVideoMaxSize(): Long {
//        return mCameraEngine.getVideoMaxSize()
//    }
//
//    /**
//     * Sets the maximum duration in milliseconds for video recordings.
//     * Once this duration is reached, the recording will automatically stop.
//     * Defaults to unlimited duration. Use 0 or negatives to disable.
//     *
//     * @param videoMaxDurationMillis The maximum video duration in milliseconds
//     */
//    fun setVideoMaxDuration(videoMaxDurationMillis: Int) {
//        mCameraEngine.setVideoMaxDuration(videoMaxDurationMillis)
//    }
//
//    /**
//     * Returns the maximum duration in milliseconds for video recordings, or 0
//     * if no limit was set.
//     *
//     * @see .setVideoMaxDuration
//     * @return the maximum duration in milliseconds
//     */
//    fun getVideoMaxDuration(): Int {
//        return mCameraEngine.getVideoMaxDuration()
//    }
//
//    /**
//     * Returns true if the camera is currently recording a video
//     * @return boolean indicating if the camera is recording a video
//     */
//    fun isTakingVideo(): Boolean {
//        return mCameraEngine.isTakingVideo()
//    }
//
//    /**
//     * Returns true if the camera is currently capturing a picture
//     * @return boolean indicating if the camera is capturing a picture
//     */
//    fun isTakingPicture(): Boolean {
//        return mCameraEngine.isTakingPicture()
//    }
//
//    /**
//     * Sets the overlay layout hardware canvas capture mode to allow hardware
//     * accelerated views to be captured in snapshots
//     *
//     * @param on true if enabled
//     */
//    fun setDrawHardwareOverlays(on: Boolean) {
//        mOverlayLayout.setHardwareCanvasEnabled(on)
//    }
//
//    /**
//     * Returns true if the overlay layout is set to capture the hardware canvas
//     * of child views
//     *
//     * @return boolean indicating hardware canvas capture is enabled
//     */
//    fun getDrawHardwareOverlays(): Boolean {
//        return mOverlayLayout.getHardwareCanvasEnabled()
//    }
//
//    //endregion
//    //region Callbacks and dispatching
//    @VisibleForTesting
//    internal inner class CameraCallbacks() : CameraEngine.Callback,
//        OrientationHelper.Callback, GestureFinder.Controller {
//        private val TAG: String = CameraCallbacks::class.java.simpleName
//        private val LOG: CameraLogger = CameraLogger.create(TAG)
//        val context: Context
//            get() {
//                return context
//            }
//        val width: Int
//            get() {
//                return width
//            }
//        val height: Int
//            get() {
//                return height
//            }
//
//        fun dispatchOnCameraOpened(options: CameraOptions) {
//            LOG.i("dispatchOnCameraOpened", options)
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    for (listener: CameraListener in mListeners) {
//                        listener.onCameraOpened(options)
//                    }
//                }
//            })
//        }
//
//        fun dispatchOnCameraClosed() {
//            LOG.i("dispatchOnCameraClosed")
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    for (listener: CameraListener in mListeners) {
//                        listener.onCameraClosed()
//                    }
//                }
//            })
//        }
//
//        fun onCameraPreviewStreamSizeChanged() {
//            // Camera preview size has changed.
//            // Request a layout pass for onMeasure() to do its stuff.
//            // Potentially this will change CameraView size, which changes Surface size,
//            // which triggers a new Preview size. But hopefully it will converge.
//            val previewSize: Size? = mCameraEngine.getPreviewStreamSize(Reference.VIEW)
//            if (previewSize == null) {
//                throw RuntimeException("Preview stream size should not be null here.")
//            } else if (previewSize.equals(mLastPreviewStreamSize)) {
//                LOG.i(
//                    "onCameraPreviewStreamSizeChanged:",
//                    "swallowing because the preview size has not changed.", previewSize
//                )
//            } else {
//                LOG.i(
//                    "onCameraPreviewStreamSizeChanged: posting a requestLayout call.",
//                    "Preview stream size:", previewSize
//                )
//                mUiHandler!!.post(object : Runnable {
//                    override fun run() {
//                        requestLayout()
//                    }
//                })
//            }
//        }
//
//        fun dispatchOnPictureShutter(shouldPlaySound: Boolean) {
//            if (shouldPlaySound && mPlaySounds) {
//                playSound(MediaActionSound.SHUTTER_CLICK)
//            }
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    for (listener: CameraListener in mListeners) {
//                        listener.onPictureShutter()
//                    }
//                }
//            })
//        }
//
//        fun dispatchOnPictureTaken(stub: PictureResult.Stub) {
//            LOG.i("dispatchOnPictureTaken", stub)
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    val result: PictureResult = PictureResult(stub)
//                    for (listener: CameraListener in mListeners) {
//                        listener.onPictureTaken(result)
//                    }
//                }
//            })
//        }
//
//        fun dispatchOnVideoTaken(stub: VideoResult.Stub) {
//            LOG.i("dispatchOnVideoTaken", stub)
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    val result: VideoResult = VideoResult(stub)
//                    for (listener: CameraListener in mListeners) {
//                        listener.onVideoTaken(result)
//                    }
//                }
//            })
//        }
//
//        fun dispatchOnFocusStart(
//            gesture: Gesture?,
//            point: PointF
//        ) {
//            LOG.i("dispatchOnFocusStart", gesture, point)
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    mMarkerLayout.onEvent(MarkerLayout.TYPE_AUTOFOCUS, arrayOf(point))
//                    if (mAutoFocusMarker != null) {
//                        val trigger: AutoFocusTrigger =
//                            if (gesture != null) AutoFocusTrigger.GESTURE else AutoFocusTrigger.METHOD
//                        mAutoFocusMarker.onAutoFocusStart(trigger, point)
//                    }
//                    for (listener: CameraListener in mListeners) {
//                        listener.onAutoFocusStart(point)
//                    }
//                }
//            })
//        }
//
//        fun dispatchOnFocusEnd(
//            gesture: Gesture?,
//            success: Boolean,
//            point: PointF
//        ) {
//            LOG.i("dispatchOnFocusEnd", gesture, success, point)
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    if (success && mPlaySounds) {
//                        playSound(MediaActionSound.FOCUS_COMPLETE)
//                    }
//                    if (mAutoFocusMarker != null) {
//                        val trigger: AutoFocusTrigger =
//                            if (gesture != null) AutoFocusTrigger.GESTURE else AutoFocusTrigger.METHOD
//                        mAutoFocusMarker.onAutoFocusEnd(trigger, success, point)
//                    }
//                    for (listener: CameraListener in mListeners) {
//                        listener.onAutoFocusEnd(success, point)
//                    }
//                }
//            })
//        }
//
//        fun onDeviceOrientationChanged(deviceOrientation: Int) {
//            LOG.i("onDeviceOrientationChanged", deviceOrientation)
//            val displayOffset: Int = mOrientationHelper.getLastDisplayOffset()
//            if (!mUseDeviceOrientation) {
//                // To fool the engine to return outputs in the VIEW reference system,
//                // The device orientation should be set to -displayOffset.
//                val fakeDeviceOrientation: Int = (360 - displayOffset) % 360
//                mCameraEngine.getAngles().setDeviceOrientation(fakeDeviceOrientation)
//            } else {
//                mCameraEngine.getAngles().setDeviceOrientation(deviceOrientation)
//            }
//            val value: Int = (deviceOrientation + displayOffset) % 360
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    for (listener: CameraListener in mListeners) {
//                        listener.onOrientationChanged(value)
//                    }
//                }
//            })
//        }
//
//        fun onDisplayOffsetChanged() {
//            if (isOpened) {
//                // We can't handle display offset (View angle) changes without restarting.
//                // See comments in OrientationHelper for more information.
//                LOG.w("onDisplayOffsetChanged", "restarting the camera.")
//                close()
//                open()
//            }
//        }
//
//        fun dispatchOnZoomChanged(newValue: Float, fingers: Array<PointF?>?) {
//            LOG.i("dispatchOnZoomChanged", newValue)
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    for (listener: CameraListener in mListeners) {
//                        listener.onZoomChanged(newValue, floatArrayOf(0f, 1f), fingers)
//                    }
//                }
//            })
//        }
//
//        fun dispatchOnExposureCorrectionChanged(
//            newValue: Float,
//            bounds: FloatArray,
//            fingers: Array<PointF?>?
//        ) {
//            LOG.i("dispatchOnExposureCorrectionChanged", newValue)
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    for (listener: CameraListener in mListeners) {
//                        listener.onExposureCorrectionChanged(newValue, bounds, fingers)
//                    }
//                }
//            })
//        }
//
//        fun dispatchFrame(frame: Frame) {
//            // The getTime() below might crash if developers incorrectly release
//            // frames asynchronously.
//            LOG.v("dispatchFrame:", frame.getTime(), "processors:", mFrameProcessors.size)
//            if (mFrameProcessors.isEmpty()) {
//                // Mark as released. This instance will be reused.
//                frame.release()
//            } else {
//                // Dispatch this frame to frame processors.
//                mFrameProcessingExecutor!!.execute(object : Runnable {
//                    override fun run() {
//                        LOG.v(
//                            "dispatchFrame: executing. Passing", frame.getTime(),
//                            "to processors."
//                        )
//                        for (processor: FrameProcessor in mFrameProcessors) {
//                            try {
//                                processor.process(frame)
//                            } catch (e: Exception) {
//                                LOG.w("Frame processor crashed:", e)
//                            }
//                        }
//                        frame.release()
//                    }
//                })
//            }
//        }
//
//        fun dispatchError(exception: CameraException?) {
//            LOG.i("dispatchError", exception)
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    for (listener: CameraListener in mListeners) {
//                        listener.onCameraError(exception)
//                    }
//                }
//            })
//        }
//
//        fun dispatchOnVideoRecordingStart() {
//            LOG.i("dispatchOnVideoRecordingStart")
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    for (listener: CameraListener in mListeners) {
//                        listener.onVideoRecordingStart()
//                    }
//                }
//            })
//        }
//
//        fun dispatchOnVideoRecordingEnd() {
//            LOG.i("dispatchOnVideoRecordingEnd")
//            mUiHandler!!.post(object : Runnable {
//                override fun run() {
//                    for (listener: CameraListener in mListeners) {
//                        listener.onVideoRecordingEnd()
//                    }
//                }
//            })
//        }
//    }
//    //endregion
//    //region Frame Processing
//    /**
//     * Adds a [FrameProcessor] instance to be notified of
//     * new frames in the preview stream.
//     *
//     * @param processor a frame processor.
//     */
//    fun addFrameProcessor(processor: FrameProcessor?) {
//        if (processor != null) {
//            mFrameProcessors.add(processor)
//            if (mFrameProcessors.size == 1) {
//                mCameraEngine.setHasFrameProcessors(true)
//            }
//        }
//    }
//
//    /**
//     * Remove a [FrameProcessor] that was previously registered.
//     *
//     * @param processor a frame processor
//     */
//    fun removeFrameProcessor(processor: FrameProcessor?) {
//        if (processor != null) {
//            mFrameProcessors.remove(processor)
//            if (mFrameProcessors.size == 0) {
//                mCameraEngine.setHasFrameProcessors(false)
//            }
//        }
//    }
//
//    /**
//     * Clears the list of [FrameProcessor] that have been registered
//     * to preview frames.
//     */
//    fun clearFrameProcessors() {
//        val had: Boolean = mFrameProcessors.size > 0
//        mFrameProcessors.clear()
//        if (had) {
//            mCameraEngine.setHasFrameProcessors(false)
//        }
//    }
//
//    /**
//     * Sets the max width for frame processing [Frame]s.
//     * This option is only supported by [Engine.CAMERA2] and will have no effect
//     * on other engines.
//     *
//     * @param maxWidth max width for frames
//     */
//    fun setFrameProcessingMaxWidth(maxWidth: Int) {
//        mCameraEngine.setFrameProcessingMaxWidth(maxWidth)
//    }
//
//    /**
//     * Sets the max height for frame processing [Frame]s.
//     * This option is only supported by [Engine.CAMERA2] and will have no effect
//     * on other engines.
//     *
//     * @param maxHeight max height for frames
//     */
//    fun setFrameProcessingMaxHeight(maxHeight: Int) {
//        mCameraEngine.setFrameProcessingMaxHeight(maxHeight)
//    }
//
//    /**
//     * The max width for frame processing frames.
//     * @see .setFrameProcessingMaxWidth
//     * @return max width
//     */
//    fun getFrameProcessingMaxWidth(): Int {
//        return mCameraEngine.getFrameProcessingMaxWidth()
//    }
//
//    /**
//     * The max height for frame processing frames.
//     * @see .setFrameProcessingMaxHeight
//     * @return max height
//     */
//    fun getFrameProcessingMaxHeight(): Int {
//        return mCameraEngine.getFrameProcessingMaxHeight()
//    }
//
//    /**
//     * Sets the [android.graphics.ImageFormat] for frame processing.
//     * Before applying you should check [CameraOptions.getSupportedFrameProcessingFormats].
//     *
//     * @param format image format
//     */
//    fun setFrameProcessingFormat(format: Int) {
//        mCameraEngine.setFrameProcessingFormat(format)
//    }
//
//    /**
//     * Returns the current frame processing format.
//     * @see .setFrameProcessingFormat
//     * @return image format
//     */
//    fun getFrameProcessingFormat(): Int {
//        return mCameraEngine.getFrameProcessingFormat()
//    }
//
//    /**
//     * Sets the frame processing pool size. This is (roughly) the max number of
//     * [Frame] instances that can exist at a given moment in the frame pipeline,
//     * excluding frozen frames.
//     *
//     * Defaults to 2 - higher values will increase the memory usage with little benefit.
//     * Can be higher than 2 if [.setFrameProcessingExecutors] is used.
//     * These values should be tuned together. We recommend setting a pool size that's equal to
//     * the number of executors plus 1, so that there's always a free Frame for the camera engine.
//     *
//     * Changing this value after camera initialization will have no effect.
//     * @param poolSize pool size
//     */
//    fun setFrameProcessingPoolSize(poolSize: Int) {
//        mCameraEngine.setFrameProcessingPoolSize(poolSize)
//    }
//
//    /**
//     * Returns the current frame processing pool size.
//     * @see .setFrameProcessingPoolSize
//     * @return pool size
//     */
//    fun getFrameProcessingPoolSize(): Int {
//        return mCameraEngine.getFrameProcessingPoolSize()
//    }
//
//    /**
//     * Sets the thread pool size for frame processing. This means that if the processing rate
//     * is slower than the preview rate, you can set this value to something bigger than 1
//     * to avoid losing frames.
//     * Defaults to 1 and this should be OK for most applications.
//     *
//     * Should be tuned depending on the task, the processor implementation, and along with
//     * [.setFrameProcessingPoolSize]. We recommend choosing a pool size that is
//     * equal to the executors plus 1.
//     * @param executors thread count
//     */
//    fun setFrameProcessingExecutors(executors: Int) {
//        if (executors < 1) {
//            throw IllegalArgumentException("Need at least 1 executor, got $executors")
//        }
//        mFrameProcessingExecutors = executors
//        val executor: ThreadPoolExecutor = ThreadPoolExecutor(
//            executors,
//            executors,
//            4,
//            TimeUnit.SECONDS,
//            LinkedBlockingQueue(),
//            object : ThreadFactory {
//                private val mCount: AtomicInteger = AtomicInteger(1)
//                override fun newThread(r: Runnable): Thread {
//                    return Thread(r, "FrameExecutor #" + mCount.getAndIncrement())
//                }
//            }
//        )
//        executor.allowCoreThreadTimeOut(true)
//        mFrameProcessingExecutor = executor
//    }
//
//    /**
//     * Returns the current executors count.
//     * @see .setFrameProcessingExecutors
//     * @return thread count
//     */
//    fun getFrameProcessingExecutors(): Int {
//        return mFrameProcessingExecutors
//    }
//
//    //endregion
//    //region Overlays
//    override fun generateLayoutParams(attributeSet: AttributeSet): LayoutParams {
//        if (!mInEditor && mOverlayLayout.isOverlay(attributeSet)) {
//            return mOverlayLayout.generateLayoutParams(attributeSet)
//        }
//        return super.generateLayoutParams(attributeSet)
//    }
//
//    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
//        if (!mInEditor && mOverlayLayout.isOverlay(params)) {
//            mOverlayLayout.addView(child, params)
//        } else {
//            super.addView(child, index, params)
//        }
//    }
//
//    override fun removeView(view: View) {
//        val params: ViewGroup.LayoutParams? = view.layoutParams
//        if (!mInEditor && (params != null) && mOverlayLayout.isOverlay(params)) {
//            mOverlayLayout.removeView(view)
//        } else {
//            super.removeView(view)
//        }
//    }
//    //endregion
//    //region Filters
//    /**
//     * Applies a real-time filter to the camera preview, if it supports it.
//     * The only preview type that does so is currently [Preview.GL_SURFACE].
//     *
//     * The filter will be applied to any picture snapshot taken with
//     * [.takePictureSnapshot] and any video snapshot taken with
//     * [.takeVideoSnapshot].
//     *
//     * Use [NoFilter] to clear the existing filter,
//     * and take a look at the [Filters] class for commonly used filters.
//     *
//     * This method will throw an exception if the current preview does not support real-time
//     * filters. Make sure you use [Preview.GL_SURFACE] (the default).
//     *
//     * @see Filters
//     *
//     * @param filter a new filter
//     */
//    fun setFilter(filter: Filter) {
//        if (mCameraPreview == null) {
//            mPendingFilter = filter
//        } else {
//            val isNoFilter: Boolean = filter is NoFilter
//            val isFilterPreview: Boolean = mCameraPreview is FilterCameraPreview
//            // If not a filter preview, we only allow NoFilter (called on creation).
//            if (!isNoFilter && !isFilterPreview) {
//                throw RuntimeException(
//                    ("Filters are only supported by the GL_SURFACE preview." +
//                            " Current preview:" + mPreview)
//                )
//            }
//            // If we have a filter preview, apply.
//            if (isFilterPreview) {
//                (mCameraPreview as FilterCameraPreview).setFilter(filter)
//            }
//            // No-op: !isFilterPreview && isNoPreview
//        }
//    }
//
//    /**
//     * Returns the current real-time filter applied to the camera preview.
//     *
//     * This method will throw an exception if the current preview does not support real-time
//     * filters. Make sure you use [Preview.GL_SURFACE] (the default).
//     *
//     * @see .setFilter
//     * @return the current filter
//     */
//    fun getFilter(): Filter {
//        if (mCameraPreview == null) {
//            return mPendingFilter
//        } else if (mCameraPreview is FilterCameraPreview) {
//            return (mCameraPreview as FilterCameraPreview).getCurrentFilter()
//        } else {
//            throw RuntimeException(
//                ("Filters are only supported by the GL_SURFACE preview. " +
//                        "Current:" + mPreview)
//            )
//        }
//    } //endregion
//
//    companion object {
//        private val TAG: String = CameraView::class.java.simpleName
//        private val LOG: CameraLogger = CameraLogger.create(TAG)
//        val PERMISSION_REQUEST_CODE: Int = 16
//        val DEFAULT_AUTOFOCUS_RESET_DELAY_MILLIS: Long = 3000
//        val DEFAULT_PLAY_SOUNDS: Boolean = true
//        val DEFAULT_USE_DEVICE_ORIENTATION: Boolean = true
//        val DEFAULT_PICTURE_METERING: Boolean = true
//        val DEFAULT_PICTURE_SNAPSHOT_METERING: Boolean = false
//        val DEFAULT_REQUEST_PERMISSIONS: Boolean = true
//        val DEFAULT_FRAME_PROCESSING_POOL_SIZE: Int = 2
//        val DEFAULT_FRAME_PROCESSING_EXECUTORS: Int = 1
//    }
//}