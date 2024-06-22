package org.epfl.diffractogram.util;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.PickRay;
import javax.media.j3d.Shape3D;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.epfl.diffractogram.model3d.Univers.Selectable;

public class Java3dUtil {
	
	
	public static abstract class Behavior extends javax.media.j3d.Behavior {

		private javax.media.j3d.WakeupOr mouseCriterion;

		@Override
		public void initialize() {
			WakeupCriterion[] mouseEvents = new WakeupCriterion[3];
			mouseEvents[0] = new javax.media.j3d.WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
			mouseEvents[1] = new javax.media.j3d.WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
			mouseEvents[2] = new javax.media.j3d.WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
			mouseCriterion = new javax.media.j3d.WakeupOr(mouseEvents);
			wakeupOn(mouseCriterion);
		}

		protected abstract void processMouseEvent(MouseEvent e);
		
		@SuppressWarnings("rawtypes")
		@Override
		public void processStimulus(Enumeration criteria) {
			javax.media.j3d.WakeupCriterion wakeup;
			while (criteria.hasMoreElements()) {
				wakeup = (WakeupCriterion) criteria.nextElement();
				if (wakeup instanceof WakeupOnAWTEvent) {
					AWTEvent[] events = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
					for (int i = 0; i < events.length; i++) {
						if (events[i] instanceof MouseEvent) {
							processMouseEvent((MouseEvent) events[i]);
						}
					}
				}
			}
			wakeupOn(mouseCriterion);
		}

	}

}
