import PropTypes from "prop-types";

export default function Card({
  title,
  children,
  className = "",
  headerAction,
  ...props
}) {
  return (
    <div
      className={`bg-[var(--surface)] border border-[var(--border)] rounded-xl p-6 ${className}`}
      {...props}
    >
      {(title || headerAction) && (
        <div className="flex items-center justify-between mb-4">
          {title && (
            <h3 className="text-lg font-semibold text-[var(--text)]">
              {title}
            </h3>
          )}
          {headerAction && <div>{headerAction}</div>}
        </div>
      )}
      {children}
    </div>
  );
}

Card.propTypes = {
  title: PropTypes.string,
  children: PropTypes.node,
  className: PropTypes.string,
  headerAction: PropTypes.node,
};
