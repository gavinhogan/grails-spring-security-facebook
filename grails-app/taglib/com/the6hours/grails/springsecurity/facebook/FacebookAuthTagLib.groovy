package com.the6hours.grails.springsecurity.facebook

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * TODO
 *
 * @since 31.03.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */

class FacebookAuthTagLib {

	static namespace = 'facebookAuth'

    static final String MARKER = 'com.the6hours.grails.springsecurity.facebook.FacebookAuthTagLib#init'

	/** Dependency injection for springSecurityService. */
	def springSecurityService

    /**
     * @attr text OPTIONAL button text, defaults to  'button.text' config value
     * @attr requirejs OPTIONAL provides ability to disable js output.
     * @attr permissions OPTIONAL permissions to require of FB user.
     */
    def button = {attrs->
        def conf = SpringSecurityUtils.securityConfig.facebook
        String buttonText = attrs.text?:conf.button.text
        List permissions = parsePermissions(attrs)
        boolean showFaces = false
        out << "<div class=\"fb-login-button\" data-scope=\"${permissions.join(', ')}\" data-show-faces=\"${showFaces}\">$buttonText</div>"

    }

    /**
     * @attr text OPTIONAL button text, defaults to  'button.text' config value
     * @attr requirejs OPTIONAL provides ability to disable js output.
     * @attr permissions OPTIONAL permissions to require of FB user.
     * @attr eventHandlers OPTIONAL map of javascript call backs for FB events of form [<FBEventName>:<LocalJSMethodName>]
     *
     */
	def connect = { attrs, body ->
        if(attrs.requirejs != 'false'){
            script(attrs)
        }
        button(attrs)
    }
    /**
     * @attr requirejs OPTIONAL provides ability to disable js output.
     * @attr eventHandlers OPTIONAL map of javascript call backs for FB events of form [<FBEventName>:<LocalJSMethodName>]
     */
    def script = {attrs->
        Boolean init = request.getAttribute(MARKER)
        if (init == null || !init) {
            def conf = SpringSecurityUtils.securityConfig.facebook
            String lang = conf.language
            def appId = conf.appId
            out << '<div id="fb-root"></div>\n'

            out << """
                <script type="text/javascript">
                    window.fbAsyncInit = function() {
                    FB.init({
                            appId  : '${appId}',
                            status : true,
                            cookie : true,
                            xfbml  : true,
                            oauth  : true
                          });"""

            writeEventHandlers(attrs)

            out << "};"

            out << '(function(d){'
            out << "var js, id = 'facebook-jssdk'; if (d.getElementById(id)) {return;}"
            out << "js = d.createElement('script'); js.id = id; js.async = true;"
            out << "js.src = \"//connect.facebook.net/${lang}/all.js\";"
            out << "d.getElementsByTagName('head')[0].appendChild(js);"
            out << '}(document));\n'
            out << '</script>\n'
            request.setAttribute(MARKER, true)
        }
    }

    def writeEventHandlers(Map attrs) {
        attrs.eventHandlers.each {event, function ->
            out << """
                     FB.Event.subscribe('$event', function(data) {
                      if(typeof $function == 'function'){ $function(FB, data) }
                      else if(console!=null){console.log('function "${function}" does not exists and has not been called for event "${event}"')}
                     });"""
        }
    }

    private List<String> parsePermissions(attrs) {
        def permissions = []
        if (attrs.permissions) {
            if (attrs.permissions instanceof Collection) {
                permissions = attrs.permissions.findAll {
                    it != null
                }.collect {
                    it.toString().trim()
                }.findAll {
                    it.length() > 0
                }
            } else {
                permissions = attrs.permissions.toString().split(',').collect { it.trim() }
            }
        }
        return permissions
    }

}