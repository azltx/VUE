package tufts.vue;

import java.awt.Shape;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.List;
import java.util.ArrayList;

/**
 * LWComponent.java
 * 
 * Light-weight component base class for creating components to be
 * rendered by the MapViewer class.
 *
 * @author Scott Fraize
 * @version 3/10/03
 */

public class LWComponent
    implements MapItem
               ,VueConstants
{
    public interface Listener extends java.util.EventListener
    {
        public void LWCChanged(LWCEvent e);
    }
    
    /**
     * getUserMapType
     * @return String the user map type id
     **/
    public String getUserMapType() {
    	return mUserMapType;
    }
    
    /**
     * setUserMapType
     * @param String the user map type id for this LWC
     **/
    public void setUserMapType( String pID) {
    	mUserMapType = pID;
    }
    
    public void setUSerPropertyValue( String pKey, Object pValue ) {
    	mUserPropertyValues.put( pKey, pValue);
    }
    
    public Object getUserProeprtyValue( String pKey) {
    	Object retValue = mUserPropertyValues.get( pKey);
    	return retValue;
    }
    
    
    public void setID(String ID)
    {
        if (this.ID != null)
            throw new IllegalStateException("Can't set ID to [" + ID + "], already set on " + this);
        //System.out.println("setID [" + ID + "] on " + this);
        this.ID = ID;
    }
    public void setLabel(String label)
    {
        setLabel0(label, true);
    }
    /** called directly by TextBox after document edit with setDocument=false */
    void setLabel0(String label, boolean setDocument)
    {
        if (label == null || this.label == label)
            return;
        if (this.label != null && this.label.equals(label))
            return;
        this.label = label;
        // todo opt: only do this if node or link
        if (labelBox == null)
            getLabelBox();
        else if (setDocument)
            getLabelBox().setText(label);
        layout();
        notify("label");
    }

    TextBox getLabelBox()
    {
        if (this.labelBox == null) {
            this.labelBox = new TextBox(this, this.label);
            // hack for LWLink label box hit detection:
            this.labelBox.setMapLocation(getCenterX(), getCenterY());
        }
        return this.labelBox;
    }
    
    public void setNotes(String notes)
    {
        // tmp hack to workaround NotePanel bugs
        if (this.links == null) {
            new Throwable("*** ATTEMPT TO SET NOTES ON A DELETED LWC " + this).printStackTrace();
            return;
        }
        
        if (notes == null) {
            this.notes = null;
        } else {
            String trimmed = notes.trim();
            if (trimmed.length() > 0)
                this.notes = trimmed;
            else
                this.notes = null;
        }
        layout();
        notify("notes");
    }
    public void setMetaData(String metaData)
    {
        this.metaData = metaData;
        notify("meta-data");
    }
    public void setCategory(String category)
    {
        this.category = category;
        notify("category");
    }
    public void setResource(Resource resource)
    {
        this.resource = resource;
        layout();
        notify("resource");
    }
    public void setResource(String urn)
    {
        if (urn == null || urn.length() == 0)
            setResource((Resource)null);
        else
            setResource(new Resource(urn));
    }
    public Resource getResource()
    {
        return this.resource;
    }
    public String getCategory()
    {
        return this.category;
    }
    public String getID()
    {
        return this.ID;
    }
    public String getLabel()
    {
        return this.label;
    }

    /** does this support a user editable label? */
    public boolean supportsUserLabel() {
        return false;
    }
    /** does this support user resizing? */
    public boolean supportsUserResize() {
        return false;
    }
    
    public boolean hasLabel()
    {
        return this.label != null;
    }
    
    public String getNotes()
    {
        return this.notes;
    }
    public boolean hasNotes()
    {
        return this.notes != null && this.notes.length() > 0;
    }
    public boolean hasResource()
    {
        return this.resource != null;
    }
    public String getMetaData()
    {
        return this.metaData;
    }
    public boolean hasMetaData()
    {
        return this.metaData != null;
    }
    public boolean inPathway()
    {
        return pathwayRefs != null && pathwayRefs.size() > 0;
    }
    void addPathwayRef(Pathway p)
    {
        if (pathwayRefs == null)
            pathwayRefs = new ArrayList();
        pathwayRefs.add(p);
        layout();
    }
    void removePathwayRef(Pathway p)
    {
        pathwayRefs.remove(p);
        layout();
    }
    

    /** for persistance */
    public String getXMLlabel()
    {
        return escapeNewlines(this.label);
    }

    /** for persistance */
    public void setXMLlabel(String text)
    {
        setLabel(unescapeNewlines(text));
    }

    /** for persistance */
    public String getXMLnotes()
    {
        return escapeNewlines(this.notes);
    }

    /** for persistance */
    public void setXMLnotes(String text)
    {
        String notes = unescapeNewlines(text);
        // hack in case castor xml indent was on when save was done
        // (didn't used to be a problem, but castor must have
        // changed how it handles this recently)
        notes = notes.replaceAll("\n            %nl;", "");
        notes = notes.replaceAll("\n            ", " ");
        setNotes(notes);
    }

    private String escapeNewlines(String text)
    {
        if (text == null)
            return null;
        else {
            text = text.replaceAll("%", "%pct;");
            return text.replaceAll("\n", "%nl;");
        }
    }
    private String unescapeNewlines(String text)
    {
        if (text == null)
            return null;
        else { 
            text = text.replaceAll("%nl;", "\n");
            return text.replaceAll("%pct;", "%");
        }
    }
    
    /**
     * If this component supports special layout for it's children,
     * or resizes based on font, label, etc, do it here.
     */
    protected void layout() {}
    
    public String OLD_toString()
    {
        String s = getClass().getName() + "[id=" + getID();
        if (getLabel() != null)
            s += " \"" + getLabel() + "\"";
        s += "]";
        return s;
    }

    /*
     * Persistent information
     */
    private static final String EMPTY = "";

    // persistent core
    private String ID = null;
    protected String label = null; // protected for debugging purposes
    private String notes = null;
    private String metaData = null;
    private String category = null;
    private Resource resource = null;
    private float x;
    private float y;
    private String mUserMapType = null;
    private java.util.Map mUserPropertyValues = new java.util.HashMap();
    
    // persistent impl
    protected float width = 10;
    protected float height = 10;

    protected Color fillColor = null;           //style
    protected Color textColor = COLOR_TEXT;     //style
    protected Color strokeColor = COLOR_STROKE; //style
    protected float strokeWidth = 0f;            //style
    //protected Font font = null;                 //style // why did we want this null?
    protected Font font = FONT_DEFAULT;
    
    /*
     * Runtime only information
     */
    protected transient TextBox labelBox = null;
    protected transient BasicStroke stroke = STROKE_ZERO;
    protected transient boolean displayed = true;
    protected transient boolean selected = false;
    protected transient boolean indicated = false;
    protected transient boolean rollover = false;
    protected transient boolean isZoomedFocus = false;

    protected transient LWContainer parent = null;

    // list of LWLinks that contain us as an endpoint
    private transient java.util.List links = new java.util.ArrayList();
    protected transient List pathwayRefs;

    // Scale currently exists ONLY to support the auto-managed child-node feature of nodes
    protected transient float scale = 1.0f;

    private transient java.util.List listeners;

    /** for save/restore only & internal use only */
    public LWComponent()
    {
        //System.out.println(Integer.toHexString(hashCode()) + " LWComponent construct of " + getClass().getName());
    }

    /** Create a component with duplicate content & style.
     * Does not duplicate any links to this component,
     * and leaves it an unparented orphan.
     */
    public LWComponent duplicate()
    {
        LWComponent c = null;

        try {
            c = (LWComponent) getClass().newInstance();
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
        c.setFillColor(getFillColor());
        c.setTextColor(getTextColor());
        c.setStrokeColor(getStrokeColor());
        c.setStrokeWidth(getStrokeWidth());
        c.font = this.font;
        c.scale = this.scale;
        c.setLabel(this.label); // use setLabel so new TextBox will be created
        c.x = this.x;
        c.y = this.y;
        c.width = this.width;
        c.height = this.height;
        
        if (hasResource())
            c.setResource(getResource());
        if (hasNotes())
            c.setNotes(getNotes());
        
        return c;
    }
    
    public Color getFillColor()
    {
        return this.fillColor;
    }
    /** Color to use at draw time.
        LWNode overrides to provide darkening of children. */
    public Color getRenderFillColor()
    {
        return getFillColor();
    }
    public void setFillColor(Color color)
    {
        this.fillColor = color;
        notify("fillColor");
    }

    /** for persistance */
    public String getXMLfillColor()
    {
        return ColorToString(getFillColor());
    }
    /** for persistance */
    public void setXMLfillColor(String xml)
    {
        setFillColor(StringToColor(xml));
    }
    
    public Color getTextColor()
    {
        return this.textColor;
    }
    public void setTextColor(Color color)
    {
        this.textColor = color;
        if (labelBox != null)
            labelBox.copyStyle(this); // todo better: handle thru style.textColor notification?
        notify("textColor");
    }
    /** for persistance */
    public String getXMLtextColor()
    {
        return ColorToString(getTextColor());
    }
    /** for persistance */
    public void setXMLtextColor(String xml)
    {
        setTextColor(StringToColor(xml));
    }
    
    public Color getStrokeColor()
    {
        return this.strokeColor;
    }
    public void setStrokeColor(Color color)
    {
        this.strokeColor = color;
        notify("strokeColor");
    }
    /** for persistance */
    public String getXMLstrokeColor()
    {
        return ColorToString(getStrokeColor());
    }
    /** for persistance */
    public void setXMLstrokeColor(String xml)
    {
        setStrokeColor(StringToColor(xml));
    }
    static String ColorToString(Color c)
    {
        if (c == null || (c.getRGB() & 0xFFFFFF) == 0)
            return null;
        //return "#" + Long.toHexString(c.getRGB() & 0xFFFFFFFF);
        return "#" + Integer.toHexString(c.getRGB() & 0xFFFFFF);
    }
    static Color StringToColor(String xml)
    {
        if (xml.trim().length() < 1)
            return null;
        
	Color c = null;
        try {
            Integer intval = Integer.decode(xml);
            //Long longval = Long.decode(xml); // transparency test -- works,just need gui
            //c = new Color(longval.intValue(), true);
            c = new Color(intval.intValue());
        } catch (NumberFormatException e) {
            System.err.println("LWComponent.StringToColor[" + xml + "] " + e);
        }
        return c;
    }
    
    public float getStrokeWidth()
    {
        return this.strokeWidth;
    }
    public void setStrokeWidth(float w)
    {
        if (this.strokeWidth != w) {
            this.strokeWidth = w;
            if (w > 0)
                this.stroke = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            else
                this.stroke = STROKE_ZERO;
            if (getParent() != null) {
                // because stroke affects bounds-width, may need to re-layout parent
                getParent().layout();
            }
            notify("strokeWidth");
        }
    }
    public Font getFont()
    {
        return this.font;
    }
    public void setFont(Font font)
    {
        this.font = font;
        if (labelBox != null)
            labelBox.copyStyle(this);
        layout();
        notify("font");
    }
    /** to support XML persistance */
    public String getXMLfont()
    {
        //if (this.font == null || this.font == getParent().getFont())
        //return null;
        
	String strStyle;
	if (font.isBold()) {
	    strStyle = font.isItalic() ? "bolditalic" : "bold";
	} else {
	    strStyle = font.isItalic() ? "italic" : "plain";
	}
        return font.getName() + "-" + strStyle + "-" + font.getSize();
      
    }
    /** to support XML persistance */
    public void setXMLfont(String xml)
    {
        setFont(Font.decode(xml));
    }
    
    public boolean isManagedColor()
    {
        // todo: either get rid of this or make it more sophisticated
        Color c = getFillColor();
        return c != null && (COLOR_NODE_DEFAULT.equals(c) || COLOR_NODE_INVERTED.equals(c));
    }
    
    /** default label X position impl: center the label in the bounding box */
    public float getLabelX()
    {
        float x = getCenterX();
        if (labelBox != null)
            x -= (labelBox.getMapWidth() / 2) + 1;
        return x;
    }
    /** default label Y position impl: center the label in the bounding box */
    public float getLabelY()
    {
        float y = getCenterY();
        if (labelBox != null)
            y -= labelBox.getMapHeight() / 2;
        return y;
    }
    
    /*
    public boolean isChild()
    {
        return this.parent != null || parent instanceof LWMap; // todo: kind of a hack
    }
    */
    void setParent(LWContainer c)
    {
        this.parent = c;
    }
    public LWContainer getParent()
    {
        return this.parent;
    }

    public boolean hasChildren()// todo: can we get rid of this?
    {
        return false;
    }

    /* for tracking who's linked to us */
    void addLinkRef(LWLink link)
    {
        if (this.links.contains(link))
            throw new IllegalStateException("addLinkRef: " + this + " already contains " + link);
        this.links.add(link);
    }
    /* for tracking who's linked to us */
    void removeLinkRef(LWLink link)
    {
        if (!this.links.remove(link))
            throw new IllegalStateException("removeLinkRef: " + this + " didn't contain " + link);
    }
    /* tell us all the links who have us as one of their endpoints */
    java.util.List getLinkRefs()
    {
        return this.links;
    }
    
    /**
     * Return an iterator over all link endpoints,
     * which will all be instances of LWComponent.
     * If this is a LWLink, it should include it's
     * own endpoints in the list.
     */
    public java.util.Iterator getLinkEndpointsIterator()
    {
        return
            new java.util.Iterator() {
                java.util.Iterator i = getLinkRefs().iterator();
                public boolean hasNext() {return i.hasNext();}
		public Object next()
                {
                    LWLink l = (LWLink) i.next();
                    LWComponent c1 = l.getComponent1();
                    LWComponent c2 = l.getComponent2();
                    // Every link, as it's connected to us, should
                    // have us as one of it's endpoints -- so return
                    // the opposite endpoint.
                    // todo: now that links can have null endpoints,
                    // this iterator can return null -- hasNext
                    // will have to get awfully fancy to handle this.
                    if (c1 == LWComponent.this)
                        return c2;
                    else
                        return c1;
                }
		public void remove() {
		    throw new UnsupportedOperationException();
                }
            };
    }
    
    /**
     * Return all LWComponents connected via LWLinks to this object.
     * Included everything except LWLink objects themselves (unless
     * it's an endpoint -- a link to a link)
     *
     * todo opt: this is repaint optimization -- when links
     * eventually know their own bounds (they know real connection
     * endpoints) we can re-do this as getAllConnections(), which
     * will can return just the linkRefs and none of the endpoints)
     */
    /*
    public java.util.List getAllConnectedNodes()
    {
        java.util.List list = new java.util.ArrayList(this.links.size());
        java.util.Iterator i = this.links.iterator();
        while (i.hasNext()) {
            LWLink l = (LWLink) i.next();
            if (l.getComponent1() != this)
                list.add(l.getComponent1());
            else if (l.getComponent2() != this) // todo opt: remove extra check eventually
                list.add(l.getComponent2());
            else
                // todo: actually, I think we want to support these
                throw new IllegalStateException("link to self on " + this);
            
        }
        return list;
    }
    */
    
    /** include all links and far endpoints of links connected to this component */
    public java.util.List getAllConnectedComponents()
    {
        java.util.List list = new java.util.ArrayList(this.links.size());
        java.util.Iterator i = this.links.iterator();
        while (i.hasNext()) {
            LWLink l = (LWLink) i.next();
            list.add(l);
            if (l.getComponent1() != this)
                list.add(l.getComponent1());
            else if (l.getComponent2() != this) // todo opt: remove extra check eventually
                list.add(l.getComponent2());
            else
                // todo: actually, I think we want to support these
                throw new IllegalStateException("link to self on " + this);
            
        }
        return list;
    }
    
    //needed for pathways to access a node's links - jay briedis
    // todo: this same as getLinkRefs
    public List getLinks(){
        return this.links;
    }

    /** get all links to us + to any descendents */
    // TODO: return immutable versions
    public List getAllLinks() {
        return getLinks();
    }
    
    public void setLinks(List links){
        this.links = links;
    }

    public LWLink getLinkTo(LWComponent c)
    {
        java.util.Iterator i = this.links.iterator();
        while (i.hasNext()) {
            LWLink l = (LWLink) i.next();
            if (l.getComponent1() == c || l.getComponent2() == c)
                return l;
        }
        return null;
    }

    public boolean hasLinkTo(LWComponent c)
    {
        return getLinkTo(c) != null;
    }
    /* supports ensure link paint order code */
    protected  LWComponent getParentWithParent(LWContainer parent)
    {
        if (getParent() == parent)
            return this;
        if (getParent() == null)
            return null;
        return getParent().getParentWithParent(parent);
    }

    void setScale(float scale)
    {
        this.scale = scale;
        notify("scale");
        //System.out.println("Scale set to " + scale + " in " + this);
    }
    
    public float getScale()
    {
        //if (parent == null || isIndicated() || parent.isIndicated())
        //return this.rollover ? 1f : this.scale;
        return this.scale;
        //return 1f;
    }
    public void translate(float dx, float dy)
    {
        setLocation(this.x + dx,
                    this.y + dy);
    }

    public void setFrame(Rectangle2D r)
    {
        if (DEBUG_LAYOUT) System.out.println("*** setFrame " + r + " " + this);
        if (r.getX() != getX() || r.getY() != getY())
            setLocation((float)r.getX(), (float)r.getY());
        setSize((float)r.getWidth(), (float)r.getHeight());
    }

    /**
     * Tell all links that have us as an endpoint that we've
     * moved or resized so the link knows to recompute it's
     * connection points.
     */
    protected void updateConnectedLinks()
    {
        java.util.Iterator i = getLinkRefs().iterator();
        while (i.hasNext()) {
            LWLink l = (LWLink) i.next();
            l.setEndpointMoved(true);
        }
    }
    
    private boolean linkNotificationDisabled = false;
    public void setLocation(float x, float y)
    {
        //System.out.println(this + " setLocation("+x+","+y+")");
        this.x = x;
        this.y = y;
        if (!linkNotificationDisabled)
            updateConnectedLinks();
        
        //notify("location"); // todo: does anyone need this?
        // also: if enable, don't forget to put in setX/getX!
    }
    
    public void setLocation(double x, double y)
    {
        setLocation((float) x, (float) y);
    }

    public void setLocation(Point2D p)
    {
        setLocation((float) p.getX(), (float) p.getY());
    }
    
    public void setCenterAt(Point2D p)
    {
        setLocation((float) p.getX() - getWidth()/2,
                    (float) p.getY() - getHeight()/2);
    }

    // special case for mapviewer rollover zooming to skip calling updateConnectedLinks
    void setCenterAtQuietly(Point2D p)
    {
        linkNotificationDisabled = true;
        setCenterAt(p);
        linkNotificationDisabled = false;
    }
    
    public Point2D getLocation()
    {
        return new Point2D.Float(this.x, this.y);
    }
    public Point2D getCenterPoint()
    {
        return new Point2D.Float(getCenterX(), getCenterY());
    }
    
    /** set component to this many pixels in size */
    public void setSize(float w, float h)
    {
        if (DEBUG_LAYOUT) System.out.println("*** LWComponent setSize " + w + "x" + h + " " + this);
        this.width = w;
        this.height = h;
        updateConnectedLinks();
    }

    /** set on screen visible component size to this many pixels in size -- used for user set size from
     * GUI interaction -- takes into account any current scale factor
     */
    public void setAbsoluteSize(float w, float h)
    {
        if (DEBUG_LAYOUT) System.out.println("*** LWComponent setAbsoluteSize " + w + "x" + h + " " + this);
        setSize(w / getScale(), h / getScale());
    }

    public float getX() { return this.x; }
    public float getY() { return this.y; }
    /** for XML restore only --todo: remove*/
    public void setX(float x) { this.x = x; }
    /** for XML restore only! --todo remove*/
    public void setY(float y) { this.y = y; }
    public float getWidth() { return this.width * getScale(); }
    public float getHeight() { return this.height * getScale(); }
    public float getBoundsWidth() { return (this.width + this.strokeWidth) * getScale(); }
    public float getBoundsHeight() { return (this.height + this.strokeWidth) * getScale(); }
    public float getCenterX() { return this.x + getWidth() / 2; }
    public float getCenterY() { return this.y + getHeight() / 2; }

    // these 4 for persistance
    public float getAbsoluteWidth() { return this.width; }
    public float getAbsoluteHeight() { return this.height; }
    public void setAbsoluteWidth(float w) { this.width = w; }
    public void setAbsoluteHeight(float h) { this.height = h; }
    
    /** return border shape of this object */
    public Shape getShape()
    {
        return getBounds();
    }
    /*
    public void setShape(Shape shape)
    {
        throw new UnsupportedOperationException("unimplemented setShape in " + this);
    }
    */

    public boolean doesRelativeDrawing() { return false; }    

    /**
     * Return bounds, including any stroke width.
     */
    public Rectangle2D getBounds()
    {
        // todo opt: cache this object?
        final Rectangle2D.Float b = new Rectangle2D.Float(this.x, this.y, getWidth(), getHeight());
        final float strokeWidth = getStrokeWidth();

        // we need this adjustment for repaint optimzation to
        // work properly -- would be a bit cleaner to compensate
        // for this in the viewer
        //if (isIndicated() && STROKE_INDICATION.getLineWidth() > strokeWidth)
        //    strokeWidth = STROKE_INDICATION.getLineWidth();

        if (strokeWidth > 0) {
            final float adj = strokeWidth / 2;
            b.x -= adj;
            b.y -= adj;
            b.width += strokeWidth;
            b.height += strokeWidth;
        }
        return b;
    }

    /**
     * Return internal bounds of the border shape, not including
     * the width of any stroked border.
     */
    public Rectangle2D getShapeBounds()
    {
        // todo opt: cache this object?
        //return new Rectangle2D.Float(this.x, this.y, getAbsoluteWidth(), getAbsoluteHeight());
        return new Rectangle2D.Float(this.x, this.y, getWidth(), getHeight());
    }
    
    /**
     * Default implementation: checks bounding box
     * Subclasses should override and compute via shape.
     */
    public boolean contains(float x, float y)
    {
        return x >= this.x && x <= (this.x+getWidth())
            && y >= this.y && y <= (this.y+getHeight());
    }
    
    /**
     * Default implementation: checks bounding box
     * Subclasses should override and compute via shape.
     */
    public boolean intersects(Rectangle2D rect)
    {
        return rect.intersects(getBounds());
    }
    
    /**
     * Does x,y fall within the selection target for this component.
     * This default impl adds a 30 pixel swath to bounding box.
     */
    public boolean targetContains(float x, float y)
    {
        final int swath = 30; // todo: preference
        float sx = this.x - swath;
        float sy = this.y - swath;
        float ex = this.x + getWidth() + swath;
        float ey = this.y + getHeight() + swath;
        
        return x >= sx && x <= ex && y >= sy && y <= ey;
    }

    /**
     * We divide area around the bounding box into 8 regions -- directly
     * above/below/left/right can compute distance to nearest edge
     * with a single subtract.  For the other regions out at the
     * corners, do a distance calculation to the nearest corner.
     * Behaviour undefined if x,y are within component bounds.
     */
    public float distanceToEdgeSq(float x, float y)
    {
        float ex = this.x + getWidth();
        float ey = this.y + getHeight();

        if (x >= this.x && x <= ex) {
            // we're directly above or below this component
            return y < this.y ? this.y - y : y - ey;
        } else if (y >= this.y && y <= ey) {
            // we're directly to the left or right of this component
            return x < this.x ? this.x - x : x - ex;
        } else {
            // This computation only makes sense following the above
            // code -- we already know we must be closest to a corner
            // if we're down here.
            float nearCornerX = x > ex ? ex : this.x;
            float nearCornerY = y > ey ? ey : this.y;
            float dx = nearCornerX - x;
            float dy = nearCornerY - y;
            return dx*dx + dy*dy;
        }
    }

    public Point2D nearestPoint(float x, float y)
    {
        float ex = this.x + getWidth();
        float ey = this.y + getHeight();
        Point2D.Float p = new Point2D.Float(x, y);

        if (x >= this.x && x <= ex) {
            // we're directly above or below this component
            if (y < this.y)
                p.y = this.y;
            else
                p.y = ey;
        } else if (y >= this.y && y <= ey) {
            // we're directly to the left or right of this component
            if (x < this.x)
                p.x = this.x;
            else
                p.x = ex;
        } else {
            // This computation only makes sense following the above
            // code -- we already know we must be closest to a corner
            // if we're down here.
            float nearCornerX = x > ex ? ex : this.x;
            float nearCornerY = y > ey ? ey : this.y;
            p.x = nearCornerX;
            p.y = nearCornerY;
        }
        return p;
    }

    public float distanceToEdge(float x, float y)
    {
        return (float) Math.sqrt(distanceToEdgeSq(x, y));
    }

    
    /**
     * Return the square of the distance from x,y to the center of
     * this components bounding box.
     */
    public float distanceToCenterSq(float x, float y)
    {
        float cx = getCenterX();
        float cy = getCenterY();
        float dx = cx - x;
        float dy = cy - y;
        return dx*dx + dy*dy;
    }
    
    public float distanceToCenter(float x, float y)
    {
        return (float) Math.sqrt(distanceToCenterSq(x, y));
    }
    
    public void draw(DrawContext dc)
    {
        draw(dc.g);
    }
    
    public void draw(java.awt.Graphics2D g)
    {
        throw new UnsupportedOperationException("unimplemented draw in " + this);
    }

    public void addLWCListener(LWComponent.Listener listener)
    {
        if (listeners == null)
            listeners = new java.util.ArrayList();
        if (listeners.contains(listener))
            new Throwable("already listening to us:" + listener + " " + this).printStackTrace();
        else
            listeners.add(listener);
    }
    public void removeLWCListener(LWComponent.Listener listener)
    {
        if (listeners == null)
            return;
        listeners.remove(listener);
    }
    public void removeAllLWCListeners()
    {
        if (listeners != null)
            listeners.clear();
    }
    public void notifyLWCListeners(LWCEvent e)
    {
        if (listeners != null) {
            java.util.Iterator i = listeners.iterator();
            while (i.hasNext()) {
                Listener l = (Listener) i.next();
                if (DEBUG_EVENTS) System.out.println(e + " -> " + l);
                try {
                    l.LWCChanged(e);
                } catch (Exception ex) {
                    System.err.println("LWComponent.notifyLWCListeners: exception during LWCEvent notification:"
                                       + "\n\tnotifying component: " + this
                                       + "\n\tevent was: " + e
                                       + "\n\tfailing listener: " + l);
                    ex.printStackTrace();
                    java.awt.Toolkit.getDefaultToolkit().beep();
                }
            }
        }

        // todo: have a seperate notifyParent? -- every parent
        // shouldn't have to be a listener

        // todo: "added" events don't need to go thru parent chain as
        // a "childAdded" event has already taken place (but
        // listeners, eg, inspectors, may need to know to see if the
        // parent changed)
        
        if (parent != null)
            parent.notifyLWCListeners(e);
    }
    
    protected void notify(String what)
    {
        // todo: we still need both src & component? (this,this)
        notifyLWCListeners(new LWCEvent(this, this, what));
    }
    
    protected void notify(String what, LWComponent c)
    {
        notifyLWCListeners(new LWCEvent(this, c, what));
    }

    /**a notify with an array of components
       added by Daisuke Fujiwara
     */
    protected void notify(String what, ArrayList componentList)
    {
        notifyLWCListeners(new LWCEvent(this, componentList, what));
    }
    
    /**
     * Do any cleanup needed now that this LWComponent has
     * been removed from the model
     */
    protected void removeFromModel()
    {
        removeAllLWCListeners();
        disconnectFromLinks();
        // help gc
        this.links.clear();
        this.links = null;
    }

    private void disconnectFromLinks()
    {
        Object[] links = this.links.toArray(); // may be modified concurrently
        for (int i = 0; i < links.length; i++) {
            LWLink l = (LWLink) links[i];
            l.disconnectFrom(this);
        }
     }
    
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    public boolean isSelected()
    {
        return this.selected;
    }
    
    public void setDisplayed(boolean displayed)
    {
        this.displayed = displayed;
    }
    public boolean isDisplayed()
    {
        return this.displayed;
    }

    public void setIndicated(boolean indicated)
    {
        if (this.indicated != indicated) {
            this.indicated = indicated;
        }
    }
    public void setRollover(boolean tv)
    {
        if (this.rollover != tv) {
            this.rollover = tv;
        }
    }
    public void setZoomedFocus(boolean tv)
    {
        if (this.isZoomedFocus != tv) {
            this.isZoomedFocus = tv;
        }
        if (getParent() != null) {
            getParent().setFocusComponent(tv ? this : null);
        }
    }

    public boolean isZoomedFocus()
    {
        return isZoomedFocus;
    }
    
    public boolean isIndicated()
    {
        return this.indicated;
    }
    public boolean isRollover()
    {
        return this.rollover;
    }

    public LWComponent findDeepestChildAt(float mapX, float mapY, LWComponent excluded)
    {
        return excluded == this ? null : this;
    }


    public void mouseEntered(MapMouseEvent e)
    {
        if (DEBUG_ROLLOVER) System.out.println("MouseEntered:     " + this);
        //e.getViewer().setIndicated(this);
        mouseOver(e);
    }
    public void mouseMoved(MapMouseEvent e)
    {
        //System.out.println("MouseMoved " + this);
        mouseOver(e);
    }
    public void mouseOver(MapMouseEvent e)
    {
        //System.out.println("MouseOver " + this);
    }
    public void mouseExited(MapMouseEvent e)
    {
        if (DEBUG_ROLLOVER) System.out.println(" MouseExited:     " + this);
        //e.getViewer().clearIndicated();
    }
    

    /** pesistance default */
    public void addObject(Object obj)
    {
        System.err.println("Unhandled XML obj: " + obj);
    }


    /** subclasses override this to add info to toString()
     (return super.paramString() + new info) */
    public String paramString()
    {
        return " " + x+","+y
            +  " " + width + "x" + height;
    }

    public String toString()
    {
        String cname = getClass().getName();
        String s = cname.substring(cname.lastIndexOf('.')+1);
        s += "[" + getID();
        if (getLabel() != null)
            s += " \"" + escapeNewlines(getLabel()) + "\"";
        if (getScale() != 1f)
            s += " z" + getScale();
        s += paramString();
        if (getResource() != null)
            s += " <" + getResource() + ">";
        s += "]";
        return s;
    }
 
    /**for hashtable usage 
       added by Daisuke Fujiwara
       Why do we need this over the built-in native hashCode?  the ID
       isn't always set and this won't work in those cases. --SF
    */
    
    public int broken_hashCode()
    {
        if (getID() != null)
          return Integer.parseInt(getID());
        
        else 
          throw new IllegalStateException("illegal null ID for the component:" + toString());
    }
    
    // There is no such thing as node "equality" -- either it's
    // the same object or it's not, so use == to compute equality. --SF
    // (or allow the default Object.equals, which does the same).
    public boolean broken_equals(LWComponent component)
    {   
        //if the object comparing with is a null pointer then return false
        if (component == null)
          return false;
        
        else
        {
            //if the ID matches
            if(getID().equals(component.getID()))
              return true;
        
            else
              return false;
        }
    }
    
    /**End of addition by Daisuke Fujiwara*/
}
