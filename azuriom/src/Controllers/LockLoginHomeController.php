<?php

namespace Azuriom\Plugin\LockLogin\Controllers;

use Azuriom\Http\Controllers\Controller;

class LockLoginHomeController extends Controller
{
    /**
     * Show the home plugin page.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
        return view('locklogin::index');
    }
}
