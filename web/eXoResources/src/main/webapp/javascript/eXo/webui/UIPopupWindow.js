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

/**
 * A class that manages a popup window
 */
eXo.webui.UIPopupWindow = {
  superClass : eXo.webui.UIPopup,
  
  /**
   * 1. Popup window 's bottom 's height is required to set correctly 'Resize'
   * button during resize process
   * 
   * 2. For unknow reasons, property 'offsetHeight' of the bottom div is not
   * accessible during resize process It's likely that the bottom 'div' is
   * locked during that period of time.
   * 
   * 3. As bottom height is unchanged across popup window (as long as it has
   * bottom), we store its height in a global variable
   */
  POPUP_WINDOW_BOTTOM_HEIGHT : 50,
  
  // TODO: manage zIndex properties
  /**
   * Shows the popup window passed in parameter gets the highest z-index
   * property of the elements in the page : . gets the z-index of the maskLayer .
   * gets all the other popup windows . gets the highest z-index from these, if
   * it's still at 0, set an arbitrary value of 2000 sets the position of the
   * popup on the page (top and left properties)
   */
  show : function(popupId, isShowMask, middleBrowser) {
    var DOMUtil = eXo.core.DOMUtil;
    var popup = document.getElementById(popupId);
    if (popup == null) return;        

    // TODO Lambkin: this statement create a bug in select box component in
    // Firefox
    // this.superClass.init(popup) ;    
    var popupBar = DOMUtil.findFirstDescendantByClass(popup, 'span', 'PopupTitle');
    popupBar.onmousedown = this.initDND;    
    
    var resizeBtn = DOMUtil.findFirstDescendantByClass(popup, "span", "ResizeButton");
    if (resizeBtn) {
    	resizeBtn.style.display = 'block';
    	resizeBtn.onmousedown = this.startResizeEvt;
    }    	

    if (isShowMask)
    	eXo.webui.UIPopupWindow.showMask(popup, true);
    popup.style.visibility = "hidden";
    this.superClass.show(popup);
    
    var iframes = DOMUtil.findDescendantsByTagName(popup, "iframe");
    if (iframes.length > 0) {
    	setTimeout(function() {eXo.webui.UIPopupWindow.setupWindow(popup, middleBrowser);}, 500);
    } else {
    	this.setupWindow(popup, middleBrowser);
    }
  },
  
  setupWindow : function(popup, middleBrowser) {	    	
	var DOMUtil = eXo.core.DOMUtil;
    var contentBlock = DOMUtil.findFirstDescendantByClass(popup, 'div', 'PopupContent');
    if (contentBlock && (eXo.core.Browser.getBrowserHeight() - 100 < contentBlock.offsetHeight)) {
      contentBlock.style.height = (eXo.core.Browser.getBrowserHeight() - 100) + "px";
    }
    
    var scrollY = 0, offsetParent = popup.offsetParent;
    if (window.pageYOffset != undefined)
      scrollY = window.pageYOffset;
    else if (document.documentElement && document.documentElement.scrollTop)
      scrollY = document.documentElement.scrollTop;
    else
      scrollY = document.body.scrollTop;
    // reference
    if (offsetParent) {
      var middleWindow = (DOMUtil.hasClass(offsetParent, "UIPopupWindow") || DOMUtil.hasClass(offsetParent, "UIWindow"));
      if (middleWindow) {
        popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + "px";
      }
      if (middleBrowser || !middleWindow) {
        popup.style.top = Math.ceil((eXo.core.Browser.getBrowserHeight() - popup.offsetHeight) / 2) + scrollY + "px";
      }
      // Todo: set popup of UIPopup always display in the center browsers in case UIMaskWorkspace
      if (eXo.core.DOMUtil.hasClass(offsetParent, "UIMaskWorkspace")) {
        popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + "px";
      }
      
      // hack for position popup alway top in IE6.
      var checkHeight = popup.offsetHeight > 300;

      if (document.getElementById("UIDockBar") && checkHeight) {
        popup.style.top = "6px";
      }
      if (eXo.core.I18n.lt)
        popup.style.left = Math.ceil((offsetParent.offsetWidth - popup.offsetWidth) / 2) + "px";
      else
        popup.style.right = Math.ceil((offsetParent.offsetWidth - popup.offsetWidth) / 2) + "px";
    }
    if (eXo.core.Browser.findPosY(popup) < 0)
      popup.style.top = scrollY + "px";
        
    popup.style.visibility = "visible";	  
  },
  
  hide : function(popupId, isShowMask) {
	var popup = document.getElementById(popupId);
	if (popup == null) return;     
    this.superClass.hide(popup);
    if (isShowMask) eXo.webui.UIPopupWindow.showMask(popup, false);
  },
  
  showMask : function(popup, isShowPopup) {
    var mask = popup.previousSibling;
    // Make sure mask is not TextNode because of previousSibling property
    if (mask && mask.className != "MaskLayer") {
      mask = null;
    }
    if (isShowPopup) {
      // Modal if popup is portal component
      if (eXo.core.DOMUtil.findAncestorByClass(popup, "PORTLET-FRAGMENT") == null) {
        if (!mask)
          eXo.core.UIMaskLayer.createMask(popup.parentNode, popup, 1);
      } else {
        // If popup is portlet's component, modal with just its parent
        if (!mask)
          eXo.core.UIMaskLayer.createMaskForFrame(popup.parentNode, popup, 1);
      }
    } else {
      if (mask)
        eXo.core.UIMaskLayer.removeMask(mask);
    }
  },  
  
  /**
   * Called when the window starts being resized sets the onmousemove and
   * onmouseup events on the portal application (not the popup) associates these
   * events with UIPopupWindow.resize and UIPopupWindow.endResizeEvt
   * respectively
   */
  startResizeEvt : function(evt) {
    // var portalApp = document.getElementById("UIPortalApplication") ;
    eXo.webui.UIPopupWindow.popupId = eXo.core.DOMUtil.findAncestorByClass(
        this, "UIPopupWindow").id;
    document.onmousemove = eXo.webui.UIPopupWindow.resize;
    document.onmouseup = eXo.webui.UIPopupWindow.endResizeEvt;
  },

  /**
   * Function called when the window is being resized . gets the position of the
   * mouse . calculates the height and the width of the window from this
   * position . sets these values to the window
   */
  resize : function(evt) {
    var targetPopup = document.getElementById(eXo.webui.UIPopupWindow.popupId);
    var content = eXo.core.DOMUtil.findFirstDescendantByClass(targetPopup,
        "div", "PopupContent");
    var isRTL = eXo.core.I18n.isRT();
    var pointerX = eXo.core.Browser.findMouseRelativeX(targetPopup, evt, isRTL);
    var pointerY = eXo.core.Browser.findMouseRelativeY(targetPopup, evt);
    var delta = eXo.core.Browser.findPosYInContainer(content, targetPopup)
        + content.style.borderWidth + content.style.padding
        + content.style.margin;
    // var
    // bottomLevel=eXo.core.DOMUtil.findDescendantsByClass(targetPopup,"div","BCPortalComposer");
    // TODO: Check if the bottom is not null before assign new value to
    // 'content.style.height'
    if ((pointerY - delta) > 0)
      content.style.height = (pointerY - delta - eXo.webui.UIPopupWindow.POPUP_WINDOW_BOTTOM_HEIGHT)
          + "px";
    targetPopup.style.height = "auto";

    if (isRTL) {
      pointerX = (-1) * pointerX;
    }

    if (pointerX > 200)
      targetPopup.style.width = (pointerX + 5) + "px";
  },

  /**
   * Called when the window stops being resized cancels the mouse events on the
   * portal app inits the scroll managers active on this page (in case there is
   * one in the popup)
   */
  endResizeEvt : function(evt) {
    delete eXo.webui.UIPopupWindow.popupId;
    this.onmousemove = null;
    this.onmouseup = null;
  },
  /**
   * Inits the drag and drop configures the DragDrop callback functions .
   * initCallback : sets overflow: hidden to elements in the popup if browser is
   * mozilla . dragCallback : empty . dropCallback : sets overflow: auto to
   * elements in the popup if browser is mozilla
   */
  initDND : function(evt) {
    var DragDrop = eXo.core.DragDrop;
    var DOMUtil = eXo.core.DOMUtil;

    DragDrop.initCallback = function(dndEvent) {
      var dragObject = dndEvent.dragObject;
      dragObject.uiWindowContent = DOMUtil.findFirstDescendantByClass(
          dragObject, "div", "PopupContent");
      if (!dragObject.uiWindowContent)
        return;
      if (eXo.core.Browser.browserType == "mozilla") {
        dragObject.uiWindowContent.style.overflow = "hidden";
        var elements = eXo.core.DOMUtil.findDescendantsByClass(
            dragObject.uiWindowContent, "ul", "PopupMessageBox");
        for ( var i = 0; i < elements.length; i++) {
          elements[i].style.overflow = "hidden";
        }
      }
    }

    DragDrop.dragCallback = function(dndEvent) {
    }

    DragDrop.dropCallback = function(dndEvent) {
      var dragObject = dndEvent.dragObject;
      if (eXo.core.Browser.browserType == "mozilla"
          && dragObject.uiWindowContent) {
        dragObject.uiWindowContent.style.overflow = "auto";
        var elements = eXo.core.DOMUtil.findDescendantsByClass(
            dragObject.uiWindowContent, "ul", "PopupMessageBox");
        for ( var i = 0; i < elements.length; i++) {
          elements[i].style.overflow = "auto";
        }
      }

      var offsetParent = dragObject.offsetParent;
      if (offsetParent) {
        if (eXo.core.Browser.findPosY(dragObject) < 0)
          dragObject.style.top = (0 - offsetParent.offsetTop) + "px";
      } else {
        dragObject.style.top = "0px";
      }
    }
    var clickBlock = this;
    var dragBlock = eXo.core.DOMUtil.findAncestorByClass(this, "UIDragObject");
    DragDrop.init(null, clickBlock, dragBlock, evt);
  }
};