package org.epfl.diffractogram.model3d;

import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.epfl.diffractogram.bottomPanel.HVPanel.SliderAndValue;

public class Animator {
	public boolean fromToEnable;
	public double from;
	public double to;
	public double speed;
	private AnimationThread currentThread;
	
	private abstract class AnimationThread extends Thread {
		public boolean hasToStop=false, running=false;
		private JToggleButton button;
		
		public AnimationThread(JToggleButton button) {
			this.button=button; 
		}
		public void run() {
			if (currentThread!=null && currentThread.running) {
				currentThread.hasToStop = true;
				while (currentThread!=null&&currentThread.running) {
					doSleep();
				}
			}
			currentThread = this;
			running = true;
			hasToStop = false;
			doLoop();
			releaseButton(button);
			running = false;
			currentThread = null;
		}
		public abstract void doLoop();
	}
	
	public void animateSingleAngle(final SliderAndValue slider, final double currentValue, JToggleButton button) {
		new AnimationThread(button) {
			public void doLoop() {
				for (double a=fromToEnable?from:currentValue; (!fromToEnable||a<=to)&&!hasToStop; a+=speed) {
					setSliderValue(slider, ((((int)Math.round(a))+180)%360)-180);
					doSleep();
				}
				if (fromToEnable && !hasToStop)
					setSliderValue(slider, ((((int)Math.round(to))+180)%360)-180);
			}
		}.start();
	}

	public void animateSequential(final SliderAndValue sliderX, final SliderAndValue sliderY, final SliderAndValue sliderZ, final double currentValueX, final double currentValueY, final double currentValueZ, JToggleButton button) {
//		final double x0 = fromToEnable?from:currentValueX;
//		final double x1 = fromToEnable?to:(currentValueX+360);
//		final double y0 = fromToEnable?from:currentValueY;
//		final double y1 = fromToEnable?to:(currentValueY+360);
//		final double z0 = fromToEnable?from:currentValueZ;
//		final double z1 = fromToEnable?to:(currentValueZ+360);
		new AnimationThread(button) {
			public void doLoop() {
				for (double x=fromToEnable?from:currentValueX; (!fromToEnable||x<=to)&&!hasToStop; x+=speed) {
					setSliderValue(sliderX, ((((int)Math.round(x))+180)%360)-180);
					for (double y=fromToEnable?from:currentValueY; (!fromToEnable||y<=to)&&!hasToStop; y+=speed) {
						setSliderValue(sliderY, ((((int)Math.round(y))+180)%360)-180);
						for (double z=fromToEnable?from:currentValueZ; (!fromToEnable||z<=to)&&!hasToStop; z+=speed) {
							setSliderValue(sliderZ, ((((int)Math.round(z))+180)%360)-180);
							doSleep();
						}
						if (fromToEnable && !hasToStop) setSliderValue(sliderZ, ((((int)Math.round(to))+180)%360)-180);
					}
					if (fromToEnable && !hasToStop) setSliderValue(sliderY, ((((int)Math.round(to))+180)%360)-180);
				}
				if (fromToEnable && !hasToStop) setSliderValue(sliderX, ((((int)Math.round(to))+180)%360)-180);
			}
		}.start();
	}

	public void animateRandom(final SliderAndValue sliderX, final SliderAndValue sliderY, final SliderAndValue sliderZ, JToggleButton button) {
		new AnimationThread(button) {
			public void doLoop() {
				while (!hasToStop) {
					int v = (int)Math.round(Math.random()*360-180);
					switch((int)Math.floor(Math.random()*3)) {
						case 0:
							setSliderValue(sliderX, v);
							break;
						case 1:
							setSliderValue(sliderY, v);
							break;
						case 2:
							setSliderValue(sliderZ, v);
							break;
					}
					doSleep();
				}
			}
		}.start();
	}
	
	public void animateLambda(final SliderAndValue slider, final double l0, final double l1, JToggleButton button) {
		new AnimationThread(button) {
			public void doLoop() {
				for (double l=l0; l<=l1&&!hasToStop; l+=speed/50.0) {
					setSliderValue(slider, l);
					doSleep();
				}
			}
		}.start();
	}

	public void animatePrecession(final SliderAndValue slider, final double currentValue, JToggleButton button) {
//		final double a0 = fromToEnable?from:currentValue;
//		final double a1 = fromToEnable?to:(currentValue+360);
		new AnimationThread(button) {
			public void doLoop() {
				for (double a=currentValue; !hasToStop; a+=speed) {
					setSliderValue(slider, ((((int)Math.round(a))+180)%360)-180);
					doSleep();
				}
			}
		}.start();
	}
	
	public void stopAnimation() {
		if (currentThread!=null && currentThread.running) {
			currentThread.hasToStop = true;
		}
	}
	
	private void releaseButton(final JToggleButton button) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (button!=null) button.setSelected(false); 
			}
		});
	}
	
	private void setSliderValue(final SliderAndValue slider, final double value) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				slider.setValue(value); 
			}
		});
	}
	
	private static void doSleep() {
		try {
			Thread.sleep(50);
		} catch (Exception e) {}
	}
}
