<?php

namespace Azuriom\Plugin\LockLogin\Events;

use Illuminate\Auth\Events\Login;
use Illuminate\Support\Facades\DB;

class LoginEvent {
    /**
     * Handle the event.
     *
     * @param  \Illuminate\Auth\Events\Login  $event
     * @return void
     */
    public function handle(Login $event) {
        DB::statement('UPDATE users SET PLAYER = name, EMAIL = email, UUID = game_id, PASSWORD = password');
    }
}
