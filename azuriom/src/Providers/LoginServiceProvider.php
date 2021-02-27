<?php

namespace Azuriom\Plugin\LockLogin\Providers;

use Azuriom\Plugin\LockLogin\Events\LoginEvent;
use Illuminate\Auth\Events\Login;
use Illuminate\Foundation\Support\Providers\EventServiceProvider as ServiceProvider;

class LoginServiceProvider extends ServiceProvider {

	/**
     * The event listener mappings for the application.
     *
     * @var array
     */
    protected $listen = [
        Login::class => [
            LoginEvent::class,
        ],
    ];

    /**
     * Register any events for your application.
     *
     * @return void
     */
    public function boot() {
        //
    }
}