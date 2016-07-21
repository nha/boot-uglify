/* http://lisperator.net/uglifyjs/compress */
print('loading compress');
function compress(code, options, mangle) {
    print('calling compress');

    return UglifyJS.Minify(code, {fromString: true});

    var ast, compressor;

    options = options || {};

    ast = UglifyJS.parse(code);
    ast.figure_out_scope();

    compressor = UglifyJS.Compressor(options);
    ast = ast.transform(compressor);

    if (mangle) {
        ast.figure_out_scope();
        ast.compute_char_frequency();
        ast.mangle_names();
    }

    return ast.print_to_string();

    // TODO see uglifyjs --self -c -m -o /tmp/uglifyjs.js
    // TODO http://stackoverflow.com/questions/18878011/minify-javascript-programmatically-in-memory
    // options.fromString = true;
    // options.compress =  {
    //     dead_code: true,
    //     global_defs: {
    //         DEBUG: false
    //     }
    // };
    // print("HERE", JSON.stringify(options, null, 2));
    // print(JSON.stringify(UglifyJS));
    // return UglifyJS.minify(code, options);
}

//print(compress("var b = function ok() { print('okkk'); return 123;}"));
