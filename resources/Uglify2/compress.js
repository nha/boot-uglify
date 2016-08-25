/* http://lisperator.net/uglifyjs/compress */
// uglify self file generated with (compress and mangle):
// to have latest version, git clone && npm link .
// uglifyjs --self -cm  > uglifyjs.self.js
//
//return UglifyJS.minify(code, {fromString: true}); // not implemented, see https://github.com/mishoo/UglifyJS2/issues/1223
// TODO see uglifyjs --self -c -m -o /tmp/uglifyjs.js
// TODO http://stackoverflow.com/questions/18878011/minify-javascript-programmatically-in-memory

print('loading compress');
function compress(code, options, mangle) {
    print('calling compress');

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
}
//print(compress("var b = function ok() { print('okkk'); return 123;}"));
