/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

function UIDashboard() {
	
	this.containerClass = "UIDashboardLayoutContainer";
	var currCol = null;	
	var targetObj = null;
	
	UIDashboard.prototype.init = function (dragItem, dragObj) {
		
		var DOMUtil = eXo.core.DOMUtil;
		eXo.core.DragDrop2.init(dragItem, dragObj);

		dragObj.onDragStart = function(x, y, lastMouseX, lastMouseY, e) {
			var uiDashboard = eXo.webui.UIDashboard ;
			var dashboardContainer = DOMUtil.findAncestorByClass(dragObj, eXo.webui.UIDashboard.containerClass);
			if(!dashboardContainer) return;
			
			var ggwidth = dragObj.offsetWidth;
			var ggheight = dragObj.offsetHeight;
			
			var uiWorkingWS = document.getElementById("UIWorkingWorkspace");
			//find position to put drag object in
			var mx = eXo.webui.UIDashboardUtil.findMouseRelativeX(uiWorkingWS, e);
			var ox = eXo.webui.UIDashboardUtil.findMouseRelativeX(dragObj, e);
			var x = mx-ox;
				
			var my = eXo.webui.UIDashboardUtil.findMouseRelativeY(uiWorkingWS, e);
			var oy = eXo.webui.UIDashboardUtil.findMouseRelativeY(dragObj, e);
			var y = my-oy;

			var temp = dragObj;
			while(temp.parentNode && DOMUtil.hasDescendant(dashboardContainer, temp)) {
				if(temp.scrollLeft>0) 
					x -= temp.scrollLeft;
				if(temp.scrollTop>0)
					y -= temp.scrollTop;
				temp = temp.parentNode;
			}
			
			var uiTarget = null;
			if(!DOMUtil.hasClass(dragObj, "SelectItem")) {
				uiTarget = uiDashboard.createTarget(ggwidth, 0);
				dragObj.parentNode.insertBefore(uiTarget, dragObj.nextSibling);
				currCol = eXo.core.DOMUtil.findDescendantsByClass(dragObj, "div", "UIColumnContainer");
			}else{
				var dragCopyObj = dragObj.cloneNode(true);
				DOMUtil.addClass(dragCopyObj, "CopyObj");
				dragObj.parentNode.insertBefore(dragCopyObj, dragObj);
				targetObj = null;
			}
			dragObj.style.width = ggwidth +"px";

			//increase speed of mouse when over iframe by create div layer above it
			var uiGadgets = DOMUtil.findDescendantsByClass(dashboardContainer, "div", "UIGadget");
			for(var i=0; i<uiGadgets.length; i++) {
				var uiMask = DOMUtil.findFirstDescendantByClass(uiGadgets[i], "div", "UIMask");
				if(uiMask!=null) {
					var gadgetApp = DOMUtil.findFirstDescendantByClass(uiGadgets[i], "div", "GadgetApplication");
					uiMask.style.marginTop = - gadgetApp.offsetHeight + "px";
					uiMask.style.height = gadgetApp.offsetHeight + "px";
					uiMask.style.width = gadgetApp.offsetWidth + "px";
					uiMask.style.display = "block";
					uiMask.style.backgroundColor = "white";
					eXo.core.Browser.setOpacity(uiMask, 3);
				}
			}
			
			if(!DOMUtil.hasClass(dragObj, "Dragging"))
				DOMUtil.addClass(dragObj, "Dragging");
				
			//set position of drag object
			dragObj.style.position = "absolute";
			eXo.webui.UIDashboardUtil.setPositionInContainer(uiWorkingWS, dragObj, x, y);
			if(uiTarget!=null) {
				uiTarget.style.height = ggheight +"px";
				targetObj = uiTarget;
			}
		}			
		
		dragObj.onDrag = function(nx, ny, ex, ey, e) {	
			var uiTarget = targetObj;
			var dashboardCont = DOMUtil.findAncestorByClass(dragObj, eXo.webui.UIDashboard.containerClass);
			if(!dashboardCont) return;		
			
//			eXo.webui.UIDashboard.scrollOnDrag(dragObj);
			if(eXo.webui.UIDashboardUtil.isIn(ex, ey, dashboardCont)) {
				if(!uiTarget) {
					uiTarget = eXo.webui.UIDashboard.createTargetOfAnObject(dragObj);
					targetObj = uiTarget;
				}
				
				var uiCol = currCol ;
				var cols = DOMUtil.findDescendantsByClass(dashboardCont, "div", "UIColumnContainer");
				
				if(!uiCol) {
					for(var i=0; i<cols.length; i++) {
						var uiColLeft = eXo.webui.UIDashboardUtil.findPosX(cols[i]) - dashboardCont.scrollLeft;
						if(uiColLeft<ex  &&  ex<uiColLeft+cols[i].offsetWidth) {
							currCol = uiCol = cols[i];
							break;
						}
					}					
				}
				
				if(!uiCol) return;

				var uiColLeft = eXo.webui.UIDashboardUtil.findPosX(uiCol) - dashboardCont.scrollLeft;
				if(uiColLeft<ex  &&  ex<uiColLeft+uiCol.offsetWidth ) {
					var windows = DOMUtil.findDescendantsByClass(uiCol, "div", "UIDragObject");
					
					//remove drag object from dropable target
					for(var i=0; i<windows.length; i++) {
						if(dragObj.id==windows[i].id) {
							windows.splice(i,1);
							break;
						}
					}

					if(windows.length == 0) {
						eXo.core.DOMUtil.findFirstDescendantByClass(uiCol, "div", "UIRowContainer").appendChild(uiTarget);
						return;
					}

					//find position and add uiTarget into column				
					for(var i=0; i<windows.length; i++) {
						var oy = eXo.webui.UIDashboardUtil.findPosY(windows[i]) + (windows[i].offsetHeight/3) - dashboardCont.scrollTop;
						
						if(ey<=oy) {
							windows[i].parentNode.insertBefore(uiTarget, windows[i]);
							break;
						}
						if(i==windows.length-1 && ey>oy) eXo.core.DOMUtil.findFirstDescendantByClass(uiCol, "div", "UIRowContainer").appendChild(uiTarget);
					}					
				}	else {
					//find column which draggin in					
					for(var i=0; i<cols.length; i++) {
						var uiColLeft = eXo.webui.UIDashboardUtil.findPosX(cols[i]) - dashboardCont.scrollLeft;
						if(uiColLeft<ex  &&  ex<uiColLeft+cols[i].offsetWidth) {
							currCol = cols[i];
							break;
						}
					}
				}
			} else {
				//prevent dragging gadget object out of DashboardContainer
				if(uiTarget!=null && DOMUtil.hasClass(dragObj, "SelectItem")) {
					uiTarget.parentNode.removeChild(uiTarget);					
					targetObj = uiTarget = null;
				}
			}
		}
	
		dragObj.onDragEnd = function(x, y, clientX, clientY) {
			var uiDashboardUtil = eXo.webui.UIDashboardUtil;
			var dashboardCont = DOMUtil.findAncestorByClass(dragObj, eXo.webui.UIDashboard.containerClass);			
			if(!dashboardCont) return;
			
			var masks = DOMUtil.findDescendantsByClass(dashboardCont, "div", "UIMask");
			for(var i=0; i<masks.length; i++) {
				eXo.core.Browser.setOpacity(masks[i], 100);
				masks[i].style.display = "none";
			}
			
			var uiTarget = targetObj;
			if(uiTarget && !uiTarget.parentNode) { 
				uiTarget = null; 
			}
			dragObj.style.position = "static";
			DOMUtil.removeClass(dragObj,"Dragging");
			
			var dragCopyObj = DOMUtil.findFirstDescendantByClass(dashboardCont, "div", "CopyObj");
			if(dragCopyObj) {
				dragCopyObj.parentNode.replaceChild(dragObj, dragCopyObj);
				dragObj.style.width = "auto";
			}
			
			if(uiTarget) {	
				var col = eXo.core.DOMUtil.findAncestorByClass(uiTarget, "UIColumnContainer");
				var row = uiDashboardUtil.findRowIndexInDashboard(uiTarget, dragObj.id);
				var compId = dashboardCont.id;
				var parent = eXo.core.DOMUtil.findAncestorByClass(uiTarget, "UIColumnContainer");
				
				if(DOMUtil.hasClass(dragObj, "SelectItem")) {
					var params = [
									{name: "columnId", value: parent.id},
									{name: "position", value: row},
									{name: "objectId", value: dragObj.id.replace(/^(UIWindow-)?(UIGadget-)?/, "")}
								];
					var url = uiDashboardUtil.createRequest(compId, 'AddNewWindow', params);
					ajaxGet(url);
				} else {
					//in case: drop to old position
					if(eXo.core.DOMUtil.findAncestorByClass(dragObj, "UIColumnContainer") == col 
								&& uiDashboardUtil.findRowIndexInDashboard(dragObj, uiTarget.id) == row) {
						uiTarget.parentNode.removeChild(uiTarget);
					} else {
						uiTarget.parentNode.replaceChild(dragObj, uiTarget);
						var params = [
										{name: "columnId", value: parent.id},
										{name: "position", value: row},
										{name: "objectId", value: dragObj.id.replace(/^(UIWindow-)?(UIGadget-)?/, "")}
									];
						var url = uiDashboardUtil.createRequest(compId, 'MoveWindow', params);
						ajaxGet(url);
					}
				}
			}

			uiTarget = DOMUtil.findFirstDescendantByClass(dashboardCont, "div", "UITarget");
			while (uiTarget) {
				DOMUtil.removeElement(uiTarget);
				uiTarget = eXo.core.DOMUtil.findFirstDescendantByClass(dashboardCont, "div", "UITarget");
			}
			targetObj = currCol = null;
		}		
		
		dragObj.onCancel = function(e){
			if(eXo.core.Browser.browserType == "ie" && eXo.core.Browser.findMouseYInClient() < 0) {
				eXo.core.DragDrop2.end(e);
			}
		}
	};
	
	UIDashboard.prototype.onLoad = function(dashboardId, canEdit) {
		var uiDashboard = document.getElementById(dashboardId);
		if(!uiDashboard) return;
		uiDashboard.style.overflow = "hidden";
		
		var DOMUtil = eXo.core.DOMUtil;		
		var selectPopup = DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "UIPopupWindow");
		if (selectPopup) {
			var closeButton = DOMUtil.findFirstDescendantByClass(selectPopup, "a", "CloseButton");	
			closeButton.onclick = eXo.webui.UIDashboard.showHideSelectContainer;			
		}
		
		eXo.webui.UIDashboard.toogleState(dashboardId);
		
		var containers = DOMUtil.findDescendantsByClass(uiDashboard, "div", "UIContainer");
		if (containers.length && !DOMUtil.hasClass(containers[0], "ApplicationDropableOnly")) {
			for (var i = 0; i < containers.length; i++) {				
				DOMUtil.addClass(containers[i], "ApplicationDropableOnly");
			}			
		}
		
		//Todo: nguyenanhkien2a@gmail.com
		//We set and increase waiting time for initDragDrop function to make sure all UI (tag, div, iframe, etc) 
		//was loaded and to avoid some potential bugs (ex: GTNPORTAL-1068)
		if (eXo.portal.portalMode === 0) {
			setTimeout("eXo.webui.UIDashboard.initDragDrop('" + dashboardId + "'," + canEdit + ");", 400) ;			
		}
	};
	
	UIDashboard.prototype.toogleState = function(dashboardId) {
		var uiDashboard = document.getElementById(dashboardId);
		if(!uiDashboard) return;
		
		var DOMUtil = eXo.core.DOMUtil;
		var noWindow = DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "NoWindow");
		if (!noWindow) return;
		
		if (!DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "GadgetApplication") &&
				!DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "UIWindow")) {
			noWindow.style.display = "block";
		} else {
			noWindow.style.display = "none";
		}
				
		if (eXo.portal.portalMode == 2 || eXo.portal.portalMode == 4) {
			setTimeout("eXo.webui.UIDashboard.toogleState(" + dashboardId + ")", 700);
		}
	}
	
	UIDashboard.prototype.initDragDrop = function(dashboardId, canEdit) {
		var DOMUtil = eXo.core.DOMUtil ;
		var uiDashboard = document.getElementById(dashboardId);
		if(!uiDashboard) return;
		
		//Init DnD for gadgets
		var dragHandles = DOMUtil.findDescendantsByClass(uiDashboard, "div", "DragHandleArea");
		for(var j=0; j<dragHandles.length; j++) {
			dragHandles[j].style.cursor = "move";
			var dragObject = DOMUtil.findAncestorByClass(dragHandles[j],"UIDragObject");
			if(canEdit) {
				eXo.webui.UIDashboard.init(dragHandles[j], dragObject);					
			} else{
				var minimizeButton = DOMUtil.findFirstDescendantByClass(dragHandles[j], "span", "MinimizeAction") ;
				if(minimizeButton) {
					minimizeButton.style.display = "none" ;
					var controlBar = minimizeButton.parentNode ;
					var closeButton = DOMUtil.findFirstChildByClass(controlBar, "span", "CloseGadget") ;
					var editButton = DOMUtil.findFirstChildByClass(controlBar, "span", "EditGadget") ;
					var maximize = DOMUtil.findFirstChildByClass(controlBar, "span", "MaximizeAction") ;
					if (maximize) maximize.style.display = "none";
					if (closeButton) closeButton.style.display = "none" ;
					if (editButton) editButton.style.display = "none" ;
				}
			}				
		}		
	};
	
	UIDashboard.prototype.initPopup = function(popup) {
		if(typeof(popup) == "string") popup = document.getElementById(popup);
		if(!popup || popup.style.display == "none") return;
		var uiDashboard = eXo.core.DOMUtil.findAncestorByClass(popup, eXo.webui.UIDashboard.containerClass);
		var deltaY = Math.ceil((uiDashboard.offsetHeight - popup.offsetHeight) / 2);
		if (deltaY < 0) {
			deltaY = 0;
		}
		popup.style.top = eXo.core.Browser.findPosY(uiDashboard) + deltaY + "px";
	};
	/**
	 * Build a UITarget element (div element) with properties in parameters
	 * @param {Number} width
	 * @param {Number} height
	 */
	UIDashboard.prototype.createTarget = function(width, height) {
		var uiTarget = document.createElement("div");
		uiTarget.id = "UITarget";
		uiTarget.className = "UITarget";
		uiTarget.style.width = width + "px";
		uiTarget.style.height = height + "px";
		return uiTarget;
	};
	 /**
   * Build a UITarget element (div element) with properties equal to object's properties in parameter
   * @param {Object} obj object
   */
	UIDashboard.prototype.createTargetOfAnObject = function(obj) {
		var uiTarget = document.createElement("div");
		uiTarget.id = "UITarget";
		uiTarget.className = "UITarget";
		uiTarget.style.height = obj.offsetHeight + "px";
		return uiTarget;
	};
	 /**
   * Show and hide gadget list for selecting gadget in dashboard
   * @param {Object} comp indicate action show and hide, if it is close button, action is hide
   */
	UIDashboard.prototype.showHideSelectContainer = function(event) {
		if(!event) event = window.event;
		var DOMUtil = eXo.core.DOMUtil;
		var comp = eXo.core.Browser.getEventSource(event);
		var uiContainer = DOMUtil.findAncestorByClass(comp, eXo.webui.UIDashboard.containerClass);
		var uiSelectPopup = DOMUtil.findFirstDescendantByClass(uiContainer, "div", "UIPopupWindow");
		var addButton = DOMUtil.findFirstDescendantByClass(uiContainer, "a", "AddIcon");

		if(uiSelectPopup.style.display != "none") {
			uiSelectPopup.style.display = "none";
			addButton.style.visibility = "visible";
			var url = eXo.webui.UIDashboardUtil.createRequest(uiContainer.id, "SetShowSelectContainer", null);
			ajaxAsyncGetRequest(url, false);
		} else {
			addButton.style.visibility = "hidden";
			var url = eXo.webui.UIDashboardUtil.createRequest(uiContainer.id, "SetShowSelectContainer", null);
			ajaxGet(url);
		}
	};
	 /**
   * Using when click event happens on a dashboard tab
   * @param {Object} clickElement
   * @param {String} normalStyle a css style
   * @param {String} selectedType a css style
   */
	UIDashboard.prototype.onTabClick = function(clickElement, normalStyle, selectedType) {
		var DOMUtil = eXo.core.DOMUtil;
		var category = DOMUtil.findAncestorByClass(clickElement, "GadgetCategory");
		var categoryContent = DOMUtil.findFirstChildByClass(category, "div", "ItemsContainer");
		var categoriesContainer = DOMUtil.findAncestorByClass(category, "GadgetItemsContainer");
		var categories = DOMUtil.findChildrenByClass(categoriesContainer, "div", "GadgetCategory");
		var gadgetTab = DOMUtil.findFirstChildByClass(category, "div", "GadgetTab");
		
		if(DOMUtil.hasClass(gadgetTab, normalStyle)) {
			for(var i=0; i<categories.length; i++) {
				DOMUtil.findFirstChildByClass(categories[i], "div", "GadgetTab").className = "GadgetTab " + normalStyle;
				DOMUtil.findFirstChildByClass(categories[i], "div", "ItemsContainer").style.display = "none";
			}
			DOMUtil.findFirstChildByClass(category, "div", "GadgetTab").className = "GadgetTab " + selectedType;
			categoryContent.style.display = "block";	
		} else {
			DOMUtil.findFirstChildByClass(category, "div", "GadgetTab").className = "GadgetTab " + normalStyle;
			categoryContent.style.display = "none";
		}
		
		var popupContent = DOMUtil.findAncestorByClass(clickElement, "PopupContent");
		if(eXo.core.Browser.getBrowserHeight() - 100 < categoriesContainer.offsetHeight) {
			popupContent.style.height = (eXo.core.Browser.getBrowserHeight() - 100) + "px";
		}	else {
			popupContent.style.height = "auto";
		}	
	};	
	
	UIDashboard.prototype.scrollOnDrag = function(dragObj) {
		var DOMUtil = eXo.core.DOMUtil;
		var dashboardUtil = eXo.webui.UIDashboardUtil;
		var uiDashboard = DOMUtil.findAncestorByClass(dragObj, "UIDashboard");
		var gadgetContainer = DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "GadgetContainer");
		var colCont = DOMUtil.findFirstChildByClass(gadgetContainer, "div", "UIColumns");
		
		if(!DOMUtil.findFirstDescendantByClass(colCont, "div", "UITarget")) return;
		
		var visibleWidth = gadgetContainer.offsetWidth;
		var visibleHeight = gadgetContainer.offsetHeight;
		var trueWidth = colCont.offsetWidth;
		var trueHeight = colCont.offsetHeight;
		
		var objLeft = dashboardUtil.findPosXInContainer(dragObj, gadgetContainer);
		var objRight = objLeft + dragObj.offsetWidth;
		var objTop = dashboardUtil.findPosYInContainer(dragObj, gadgetContainer);
		var objBottom = objTop + dragObj.offsetHeight;
		
		//controls horizontal scroll
		var deltaX = gadgetContainer.scrollLeft;
		if((trueWidth - (visibleWidth + deltaX) > 0) && objRight > visibleWidth) {
			gadgetContainer.scrollLeft += 5;
		} else {
			if(objLeft < 0 && deltaX > 0) gadgetContainer.scrollLeft -= 5;
		}
		
		//controls vertical scroll
		var controlBar = DOMUtil.findFirstChildByClass(gadgetContainer, "div", "ContainerControlBarL");
		var buttonHeight = 0 ;
		if(controlBar) buttonHeight = controlBar.offsetHeight;
		var deltaY = gadgetContainer.scrollTop;
		if((trueHeight - (visibleHeight -10 - buttonHeight + deltaY) > 0) && objBottom > visibleHeight) {
			gadgetContainer.scrollTop += 5;
		}	else {
			if(objTop < 0 && deltaY > 0) gadgetContainer.scrollTop -= 5;
		}
	};
};

eXo.webui.UIDashboard = new UIDashboard();