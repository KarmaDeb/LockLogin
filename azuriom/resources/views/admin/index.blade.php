@extends('admin.layouts.admin')

@section('title', 'How to start?')

@section('content')
    <div class="card shadow mb-4">
        <div class="card-body">
            <p>Download <a href="https://www.spigotmc.org/resources/gsa-locklogin.75156/" target="_blank">LockLogin</a> from spigot</p>
            <p>In your minecraft server, edit the file <code>/plugins/LockLogin/config.yml</code></p><br>
            <code>
                Encryption: <br>
                   ‍    ‍ Passwords: '{{config('hashing.driver')}}' <br>
                <br>
                <ins style="color: #808080; text-decoration: none;">( DO NOT ADD TIHS ) Your panel type:  <ins style="color: #32a8a8; text-decoration: none">{{config('azuriom.game')}}</ins></ins> <br>
                Azuriom: <br>
                   ‍    ‍ Restrict: true ( Optional ) <br>
                   ‍    ‍ SemiPremium: true|false ( It depends if you make your panel mc-online )<br><br>
                ... <br>
                ... <br>
                AccountSys: '{{config('database.default')}}' <br>
            </code> <br><br>
            <p>Then modify <code>/plugins/LockLogin/mysql.yml</code></p><br>
            <code>
                MySQL:  <br>
                   ‍    ‍ host: '{{config('database.connections.mysql.host')}}' <br>
                   ‍    ‍ database: '{{config('database.connections.mysql.database')}}' <br>
                   ‍    ‍ port: {{config('database.connections.mysql.port')}} <br>
                   ‍    ‍ table: users <br>
                   ‍   ‍  password: '{{config('database.connections.mysql.password')}}' <br>
            </code><br>
        </div>
    </div>
@endsection
