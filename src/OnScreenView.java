package nz.gen.geek_central.gles2_sample;
/*
    Direct onscreen display of sample animation--the GLSurfaceView where the
    animation takes place.

    Copyright 2012 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
*/

import javax.microedition.khronos.opengles.GL10;

public class OnScreenView extends android.opengl.GLSurfaceView
  {
    public android.widget.TextView StatsView;
    boolean Shaded, NewShaded; /* fixme: need to preserve last-set value across orientation changes */
    int LastViewWidth = 0, LastViewHeight = 0;
    long ThisRun, LastRun, LastTimeTaken;

    private class OnScreenViewRenderer implements Renderer
      {
      /* Note I ignore the passed GL10 argument, and exclusively use
        static methods from GLES20 class for all OpenGL drawing */
        SpinningArrow ArrowShape;

        public void onDrawFrame
          (
            GL10 _gl
          )
          {
            if (NewShaded != Shaded)
              {
                if (ArrowShape != null)
                  {
                    ArrowShape.Release();
                  } /*if*/
                ArrowShape = null; /* allocate a new one */
                Shaded = NewShaded;
              } /*if*/
            if (ArrowShape == null)
              {
                ArrowShape = new SpinningArrow(Shaded);
                ArrowShape.Setup(LastViewWidth, LastViewHeight);
              } /*if*/
            ThisRun = android.os.SystemClock.uptimeMillis();
            ArrowShape.Draw();
            LastTimeTaken = android.os.SystemClock.uptimeMillis() - ThisRun;
            LastRun = ThisRun;
            if (StatsView != null)
              {
                final String Stats = String.format
                  (
                    "%dms@%.2ffps",
                    LastTimeTaken,
                    1000.0 / (ThisRun - LastRun)
                  );
                getHandler().post
                  (
                    new Runnable()
                      {
                        public void run()
                          {
                            StatsView.setText(Stats);
                          } /*run*/
                      } /*Runnable*/
                  );
              } /*if*/
          } /*onDrawFrame*/

        public void onSurfaceChanged
          (
            GL10 _gl,
            int ViewWidth,
            int ViewHeight
          )
          {
            LastViewWidth = ViewWidth;
            LastViewHeight = ViewHeight;
            if (ArrowShape != null)
              {
                ArrowShape.Setup(ViewWidth, ViewHeight);
              } /*if*/
          } /*onSurfaceChanged*/

        public void onSurfaceCreated
          (
            GL10 _gl,
            javax.microedition.khronos.egl.EGLConfig Config
          )
          {
            ArrowShape = new SpinningArrow(Shaded);
          /* leave actual setup to onSurfaceChanged */
          } /*onSurfaceCreated*/

      } /*OnScreenViewRenderer*/

    final OnScreenViewRenderer Render = new OnScreenViewRenderer();

    public OnScreenView
      (
        android.content.Context TheContext,
        android.util.AttributeSet TheAttributes
      )
      {
        super(TheContext, TheAttributes);
        Shaded = true; /* default */
        NewShaded = Shaded;
        setEGLContextClientVersion(2);
        setRenderer(Render);
      /* setRenderMode(RENDERMODE_CONTINUOUSLY); */ /* default */
      } /*OnScreenView*/

    public boolean GetShaded()
      {
        return
            Shaded;
      } /*GetShaded*/

    public void SetShaded
      (
        final boolean NewShaded
      )
      {
        if (this.NewShaded != NewShaded)
          {
            queueEvent
              (
                new Runnable()
                  {
                    public void run()
                      {
                      /* note I don't dispose of ArrowShape here, even though I'm
                        on the GL thread, just in case the GL context is not actually
                        set properly */
                        OnScreenView.this.NewShaded = NewShaded;
                      } /*run*/
                  } /*Runnable*/
              );
          } /*if*/
      } /*Reset*/

    @Override
    public void onPause()
      {
        super.onPause();
        Render.ArrowShape = null; /* losing the GL context anyway */
      } /*onPause*/

  } /*OnScreenView*/
