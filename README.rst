Twitter Authenticator Plug-in
=============================
   
.. image:: https://img.shields.io/badge/quality-production-green
    :target: https://curity.io/resources/code-examples/status/

.. image:: https://img.shields.io/badge/availability-bundled-green
    :target: https://curity.io/resources/code-examples/status/


This project provides an opens source Twitter Authenticator plug-in for the Curity Identity Server. This allows an administrator to add functionality to Curity which will then enable end users to login using their Twitter credentials. The app that integrates with Curity may also be configured to receive the Twitter access token and refresh token, allowing it to manage resources in Twitter.

System Requirements
~~~~~~~~~~~~~~~~~~~

* Curity Identity Server 2.4.0+ and `its system requirements <https://developer.curity.io/docs/latest/system-admin-guide/system-requirements.html>`_

Requirements for Building from Source
"""""""""""""""""""""""""""""""""""""

* Maven 3
* Java JDK v. 8

Compiling the Plug-in from Source
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The source is very easy to compile. To do so from a shell, issue this command: ``mvn package``.

This will place the package, along with the relevant dependencies, in the ``target/libs`` directory.

Installation
~~~~~~~~~~~~

To install this plug-in, either download a binary version available from the `releases section of this project's GitHub repository <https://github.com/curityio/twitter-authenticator/releases>`_ or compile it from source (as described above). If you compiled the plug-in from source, the package will be placed in the ``target/libs`` subdirectory. The resulting JAR file or the one downloaded from GitHub needs to placed in the directory ``${IDSVR_HOME}/usr/share/plugins/twitter``. (The name of the last directory, ``twitter``, which is the plug-in group, is arbitrary and can be anything.) After doing so, the plug-in will become available as soon as the node is restarted.

.. note::

    The JAR file needs to be deployed to each run-time node and the admin node. For simple test deployments where the admin node is a run-time node, the JAR file only needs to be copied to one location.

The following dependent JAR file must be in the same directory, the plugin group directory:

- `scribejava-core-7.1.1.jar <https://repo1.maven.org/maven2/com/github/scribejava/scribejava-core/7.1.1/scribejava-core-7.1.1.jar>`_

After running ``mvn package``, this will be placed into the ``target/libs`` directory and can be copied from there to the plugin group directory.

For a more detailed explanation of installing plug-ins, refer to the `Curity developer guide <https://developer.curity.io/docs/latest/developer-guide/plugins/index.html#plugin-installation>`_.

Creating an App in Twitter
~~~~~~~~~~~~~~~~~~~~~~~~~~

As `described in the Twitter documentation <https://developer.twitter.com/en/docs/basics/authentication/overview/oauth>`_, you can `create apps <https://apps.twitter.com>`_ that use the Twitter APIs as shown in the following figure:

    .. figure:: docs/images/create-twitter-app1.png
        :name: doc-new-twitter-app
        :align: center
        :width: 500px

    Fill in the name, description and website and save changes.

As you create it, you'll be shown the ``API Key`` and ``API secret Key``. You'll need these later when configuring the plug-in in Curity.

From this page, you need to enable OAuth by clicking on the ``Edit`` button next to ``Authentication settings``. From there, click on ``Enable 3-legged OAuth``. You may toggle on ``Request email address from users`` as well. Configure the callback URL. To obtain this, you'll need the `endpoint of the server <https://curity.io/resources/tutorials/howtos/concepts/endpoints/>`_ and the final part of the URL will be the ID of the Twitter authenticator and the string ``callback``. An example configuration is shown in the following fiture:

    .. figure:: docs/images/oauth-settings.png
        :align: center
        :width: 500px

Also, on the main app page in the Twitter developer portal, you can manage permissions in the ``Permissions`` section as show in below figure:

    .. figure:: docs/images/twitter-update-permissions.png
        :align: center
        :width: 500px

Creating a Twitter Authenticator in Curity
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The easiest way to configure a new Twitter authenticator is using the Curity admin UI. The configuration for this can be downloaded as XML or CLI commands later, so only the steps to do this in the GUI will be described.

1. Go to the ``Authenticators`` page of the authentication profile wherein the authenticator instance should be created.
2. Click the ``New Authenticator`` button.
3. Enter a name (e.g., ``twitter1``). This name needs to match the URI component in the callback URI set in the Twitter app.
4. For the type, pick the ``Twitter`` option:

    .. figure:: docs/images/twitter-authenticator-type-in-curity.png
        :align: center
        :width: 600px

5. On the next page, you can define all of the standard authenticator configuration options like any previous authenticator that should run, the resulting ACR, transformers that should executed, etc. At the top of the configuration page, the Twitter-specific options can be found.

    .. note::

        The Twitter-specific configuration is generated dynamically based on the `configuration model defined in the Java interface <https://github.com/curityio/twitter-authenticator/blob/master/src/main/java/io/curity/identityserver/plugin/twitter/config/TwitterAuthenticatorPluginConfig.java>`_.

6. In the ``API Key`` textfield, enter the ``API Key`` from the Twitter client app.
7. Also enter the matching ``API secret Key``.

Once all of these changes are made, they will be staged, but not committed (i.e., not running). To make them active, click the ``Commit`` menu option in the ``Changes`` menu. Optionally enter a comment in the ``Deploy Changes`` dialogue and click ``OK``.

Once the configuration is committed and running, the authenticator can be used like any other.

License
~~~~~~~

This plugin and its associated documentation is listed under the `Apache 2 license <LICENSE>`_. Dependencies have their own licenses. Refer to the home page of those projects for details.

More Information
~~~~~~~~~~~~~~~~

Please visit `curity.io <https://curity.io/>`_ for more information about the Curity Identity Server.

Copyright (C) 2020 Curity AB.
