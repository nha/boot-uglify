// 1.
if (foo) {
    {{{}}}
    if (bar) { baz(); }
    {{}}
} else {
    stuff();
}

// 2.
if (foo) {
    for (var i = 0; i < 5; ++i)
        if (bar) baz();
} else {
    stuff();
}
