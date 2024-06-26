package org.epfl.diffractogram.model3d;

import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.epfl.diffractogram.gui.HVPanel.SliderAndValue;
import org.epfl.diffractogram.util.WorldRenderer;

import javajs.async.SwingJSUtils.StateHelper;
import javajs.async.SwingJSUtils.StateMachine;

/**
 * BH: This class will require a StateMachine implementation.
 * 
 *
 */
public class Animator {
	public static final int STATE_INIT = 0;
	public static final int STATE_LOOP = 1;
	public static final int STATE_DONE = 2;
	public boolean fromToEnable;
	public double from;
	public double to;
	public double speed;
	private AnimationThread currentThread;

	private abstract class AnimationThread extends Thread {

		protected static final int DELAY_MS = 50;
		StateHelper helper;
		public boolean hasToStop = false, running = false;
		private boolean isJS = WorldRenderer.isJS;
		private JToggleButton button;

		private StateMachine state = new StateMachine() {

			@Override
			public boolean stateLoop() {
				if (helper.isInterrupted())
					return true;
				switch (helper.getState()) {
				case STATE_INIT:
					running = true;
					hasToStop = false;
					initializing();
					helper.setState(STATE_LOOP);
					break;
				case STATE_LOOP:
					if (isContinuing())
						looping();
					break;
				case STATE_DONE:
					done();
					releaseButton(button);
					running = false;
					currentThread = null;
					return true;
				}
				if (!isContinuing()) {
					helper.setState(STATE_DONE);
				}
				helper.sleep(DELAY_MS);
				return false;
			}

		};

		public AnimationThread(JToggleButton button) {
			this.button = button;
			helper = new StateHelper(state);
		}

		public void run() {
			if (currentThread != null && currentThread.running) {
				currentThread.hasToStop = true;
				if (isJS) {
					helper.setNextState(STATE_DONE);
					currentThread.stop();
				} else {
					while (currentThread != null && currentThread.running) {
						doSleep();
					}
				}
			}
			currentThread = this;
			helper.next(STATE_INIT);
		}

//		public abstract void doLoop();
		public abstract void initializing();
		public abstract void looping();
		public abstract void done();
		public abstract boolean isContinuing();

	}

	public void animateSingleAngle(final SliderAndValue slider, final double currentValue, JToggleButton button) {
		new AnimationThread(button) {

			private double a;

			@Override
			public void initializing() {
				a = (fromToEnable ? from : currentValue);
			}

			@Override
			public void looping() {
				setSliderValue(slider, ((((int) Math.round(a)) + 180) % 360) - 180);
				a += speed;
			}

			@Override
			public void done() {
				if (fromToEnable && !hasToStop)
					setSliderValue(slider, ((((int) Math.round(to)) + 180) % 360) - 180);
			}

			@Override
			public boolean isContinuing() {
				return (!fromToEnable || a <= to) && !hasToStop;
			}

//			public void doLoop() {
//
//				for (double a = fromToEnable ? from : currentValue; (!fromToEnable || a <= to)
//						&& !hasToStop; a += speed) {
//					setSliderValue(slider, ((((int) Math.round(a)) + 180) % 360) - 180);
//					doSleep();
//				}
//				if (fromToEnable && !hasToStop)
//					setSliderValue(slider, ((((int) Math.round(to)) + 180) % 360) - 180);
//			}

		}.start();
	}

	public void animateSequential(final SliderAndValue sliderX, final SliderAndValue sliderY,
			final SliderAndValue sliderZ, final double currentValueX, final double currentValueY,
			final double currentValueZ, JToggleButton button) {
//		final double x0 = fromToEnable?from:currentValueX;
//		final double x1 = fromToEnable?to:(currentValueX+360);
//		final double y0 = fromToEnable?from:currentValueY;
//		final double y1 = fromToEnable?to:(currentValueY+360);
//		final double z0 = fromToEnable?from:currentValueZ;
//		final double z1 = fromToEnable?to:(currentValueZ+360);
		new AnimationThread(button) {
			private double x = Integer.MIN_VALUE, x0, y = Integer.MIN_VALUE, y0 = 0, z = Integer.MIN_VALUE, z0;

			@Override
			public void initializing() {
				x0 = (fromToEnable ? from : currentValueX);
				y0 = (fromToEnable ? from : currentValueY);
				z0 = (fromToEnable ? from : currentValueZ);
				helper.setLevel('x');
			}

			@Override
			public void looping() {
				while (true) {
					switch (helper.getLevel()) {
					case 'x':
						if (x == Integer.MIN_VALUE) {
							x = x0;
						} else {
							x += speed;
						}
						if (isContinuing()) {
							setSliderValue(sliderX, ((((int) Math.round(x)) + 180) % 360) - 180);
							y = Integer.MIN_VALUE;
							helper.setLevel('y');
							continue;
						}
						helper.setState(STATE_DONE);
						break;
					case 'y':
						if (y == Integer.MIN_VALUE) {
							y = y0;
						} else {
							y += speed;
						}
						if (isContinuing() && (!fromToEnable || y <= to)) {
							setSliderValue(sliderY, ((((int) Math.round(y)) + 180) % 360) - 180);
							z = Integer.MIN_VALUE;
							helper.setLevel('z');
							continue;
						}
						helper.setLevel('x');
						break;
					case 'z':
						if (z == Integer.MIN_VALUE) {
							z = z0;
						} else {
							z += speed;
						}
						if (isContinuing() && (!fromToEnable || z <= to)) {
							setSliderValue(sliderZ, ((((int) Math.round(z)) + 180) % 360) - 180);
							break;
						}
						helper.setLevel('y');
						break;
					}
					break;
				}
			}

			@Override
			public void done() {
			}

			@Override
			public boolean isContinuing() {
				return (!fromToEnable || x <= to) && !hasToStop;
			}

//			public void doLoop() {
//				// BH note: This doesn't actually work if !fromToEnable. In that case, 
//				// it just leaves the first two at their currentValue and cycles the last (phi)
//				for (double x = fromToEnable ? from : currentValueX; (!fromToEnable || x <= to)
//						&& !hasToStop; x += speed) {
//					setSliderValue(sliderX, ((((int) Math.round(x)) + 180) % 360) - 180);
//					for (double y = fromToEnable ? from : currentValueY; (!fromToEnable || y <= to)
//							&& !hasToStop; y += speed) {
//						setSliderValue(sliderY, ((((int) Math.round(y)) + 180) % 360) - 180);
//						for (double z = fromToEnable ? from : currentValueZ; (!fromToEnable || z <= to)
//								&& !hasToStop; z += speed) {
//							setSliderValue(sliderZ, ((((int) Math.round(z)) + 180) % 360) - 180);
//							doSleep();
//						}
//						if (fromToEnable && !hasToStop)
//							setSliderValue(sliderZ, ((((int) Math.round(to)) + 180) % 360) - 180);
//					}
//					if (fromToEnable && !hasToStop)
//						setSliderValue(sliderY, ((((int) Math.round(to)) + 180) % 360) - 180);
//				}
//				if (fromToEnable && !hasToStop)
//					setSliderValue(sliderX, ((((int) Math.round(to)) + 180) % 360) - 180);
//			}
//

		}.start();
	}

	public void animateRandom(final SliderAndValue sliderX, final SliderAndValue sliderY, final SliderAndValue sliderZ,
			JToggleButton button) {
		new AnimationThread(button) {

			@Override
			public void initializing() {
			}

			@Override
			public void looping() {
				int v = (int) Math.round(Math.random() * 360 - 180);
				switch ((int) Math.floor(Math.random() * 3)) {
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
			}

			@Override
			public void done() {
			}

			@Override
			public boolean isContinuing() {
				return !hasToStop;
			}

//			public void doLoop() {
//				while (!hasToStop) {
//					int v = (int) Math.round(Math.random() * 360 - 180);
//					switch ((int) Math.floor(Math.random() * 3)) {
//					case 0:
//						setSliderValue(sliderX, v);
//						break;
//					case 1:
//						setSliderValue(sliderY, v);
//						break;
//					case 2:
//						setSliderValue(sliderZ, v);
//						break;
//					}
//					doSleep();
//				}
//			}

		}.start();
	}

	public void animateLambda(final SliderAndValue slider, final double l0, final double l1, JToggleButton button) {
		new AnimationThread(button) {
			double l;

			@Override
			public void initializing() {
				l = 10;
			}

			@Override
			public void looping() {
				if (isContinuing())
					setSliderValue(slider, l);
				l += speed / 50.0;
			}

			@Override
			public void done() {
			}

			@Override
			public boolean isContinuing() {
				return l <= l1 && !hasToStop;
			}
			
//			public void doLoop() {
//				for (double l = l0; l <= l1 && !hasToStop; l += speed / 50.0) {
//					setSliderValue(slider, l);
//					doSleep();
//				}
//			}


		}.start();
	}

	public void animatePrecession(final SliderAndValue slider, final double currentValue, JToggleButton button) {
//		final double a0 = fromToEnable?from:currentValue;
//		final double a1 = fromToEnable?to:(currentValue+360);
		new AnimationThread(button) {
			double a;
			
			@Override
			public void initializing() {
				a = currentValue;
			}

			@Override
			public void looping() {
				setSliderValue(slider, ((((int) Math.round(a)) + 180) % 360) - 180);
				a += speed;
			}

			@Override
			public void done() {
			}

			@Override
			public boolean isContinuing() {
				return !hasToStop;
			}
			
//			public void doLoop() {
//				for (double a = currentValue; !hasToStop; a += speed) {
//					setSliderValue(slider, ((((int) Math.round(a)) + 180) % 360) - 180);
//					doSleep();
//				}
//			}


		}.start();
	}

	public void stopAnimation() {
		if (currentThread != null && currentThread.running) {
			currentThread.hasToStop = true;
		}
	}

	private void releaseButton(final JToggleButton button) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (button != null)
					button.setSelected(false);
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
		} catch (Exception e) {
		}
	}
}
