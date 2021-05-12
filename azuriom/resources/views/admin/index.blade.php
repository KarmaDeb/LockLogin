@extends('admin.layouts.admin')

@section('title', 'How to start?')

@section('content')
    <div class="card shadow mb-4">
        <div class="card-body">
            <p>Download <a href="https://www.spigotmc.org/resources/gsa-locklogin.75156/" target="_blank">LockLogin</a> from spigot</p>
            <p>Download <a href="https://karmaconfigs.github.io/updates/LockLogin/modules/sql/LockLoginSQL.jar">LockLoginSQL module</a> to enable azuriom support</p>
            <p>1 - Put LockLogin.jar in yourServer/plugins/</p>
            <p>2 - Start your server so LockLogin generates its folders</p>
            <p>3 - Stop your server</p>
            <p>4 - Put LockLoginSQL.jar in yourServer/plugins/LockLogin/plugin/modules/ ( if you are in legacy LockLogin: plugins/LockLogin/modules )<p>
            <p>5 - Edit the file <code>/plugins/LockLogin/config.yml</code></p><br>
            <code>
                Encryption: <br>
                   ‍    ‍ Passwords: '{{config('hashing.driver')}}' <br>
            </code><br>
            <p>6 - Edit the file <code>/plugins/LockLogin/plugin/modules/LockLoginSQL.yml/config.yml</code> ( if you are in legacy LockLogin: <code>plugins/LockLogin/modules/LockLoginSQL/config.yml</code> )<br>
               ‍    ‍ <ins style="color: #1dc489; text-decoration: none; font-size: 19px">Make sure SemiPremium matches with your panel mc-online option [ mc-online = true, mc-offline = false]<br>
               ‍    ‍ Panel type: <ins style="color: #32a8a8; text-decoration: none">{{config('azuriom.game')}}</ins></ins></p><br>
            <code>
                MySQL:  <br>
                   ‍    ‍ Enabled: true<br>
                   ‍    ‍ Restricted: true ( optional if you want the users to be registered in this azuriom website to join the server )<br>
                   ‍    ‍ SemiPremium: true|false ( it depends if your panel is online or offline )<br>
                   ‍    ‍ host: '{{config('database.connections.mysql.host')}}' <br>
                   ‍    ‍ database: '{{config('database.connections.mysql.database')}}' <br>
                   ‍    ‍ port: {{config('database.connections.mysql.port')}} <br>
                   ‍    ‍ table: users <br>
                   ‍   ‍  password: '{{config('database.connections.mysql.password')}}' <br>
            </code><br>
        </div>
    </div>
@endsection
